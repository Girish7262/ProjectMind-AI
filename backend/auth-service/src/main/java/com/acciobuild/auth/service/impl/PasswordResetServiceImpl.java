package com.acciobuild.auth.service.impl;

import com.acciobuild.auth.dto.ForgotPasswordRequest;
import com.acciobuild.auth.dto.ResetPasswordRequest;
import com.acciobuild.auth.entity.PasswordResetToken;
import com.acciobuild.auth.entity.User;
import com.acciobuild.auth.repository.PasswordResetRepository;
import com.acciobuild.auth.repository.UserRepository;
import com.acciobuild.auth.service.*;
import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.common.exception.BusinessException;
import com.acciobuild.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service implementation managing forgot password recovery tokens lifecycle and credentials resets.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetRepository passwordResetRepository;
    private final PasswordPolicyService passwordPolicyService;
    private final EmailService emailService;
    private final AuditService auditService;
    private final RedisTokenService redisTokenService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${app.security.password-reset.token-expiry-minutes:60}")
    private long tokenExpiryMinutes;

    private static final int RATE_LIMIT_MAX_ATTEMPTS = 5;
    private static final int RATE_LIMIT_WINDOW_MINUTES = 15;

    /**
     * Helper method to enforce rate limiting using Redis.
     */
    private void enforceRateLimit(String identifier, String actionType) {
        String key = String.format("ratelimit:%s:%s", actionType, identifier);
        Object existingVal = redisTemplate.opsForValue().get(key);
        
        int attempts = 0;
        if (existingVal != null) {
            try {
                if (existingVal instanceof Integer) {
                    attempts = (Integer) existingVal;
                } else {
                    attempts = Integer.parseInt(existingVal.toString());
                }
            } catch (NumberFormatException e) {
                log.error("Failed to parse rate limit attempts for key: {}", key, e);
            }
        }

        if (attempts >= RATE_LIMIT_MAX_ATTEMPTS) {
            log.warn("Rate limit exceeded for identifier: {} on action: {}", identifier, actionType);
            throw new BusinessException(
                    "Too many attempts. Rate limit exceeded. Please try again after " + RATE_LIMIT_WINDOW_MINUTES + " minutes.",
                    "TOO_MANY_REQUESTS"
            );
        }

        if (existingVal == null) {
            redisTemplate.opsForValue().set(key, 1, Duration.ofMinutes(RATE_LIMIT_WINDOW_MINUTES));
        } else {
            redisTemplate.opsForValue().increment(key);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<Void> forgotPassword(ForgotPasswordRequest request, String ipAddress, String device, String browser) {
        log.info("Processing forgot password request for email: {}", request.getEmail());

        // Enforce rate limiting by both email and IP address
        enforceRateLimit(request.getEmail(), "forgot-password-email");
        enforceRateLimit(ipAddress, "forgot-password-ip");

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    auditService.logEvent(null, "AUTH_FORGOT_PASSWORD_FAILED", "WARN", 
                            "Password reset requested for non-existent email: " + request.getEmail(), null);
                    return new ResourceNotFoundException("User not found with email: " + request.getEmail(), "USER_NOT_FOUND");
                });

        // Revoke any previous active forgot-password tokens for this user (replay protection)
        passwordResetRepository.invalidateExistingTokens(user.getId());

        // Generate secure random UUID token
        String tokenString = UUID.randomUUID().toString();

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(tokenString);
        resetToken.setUser(user);
        resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(tokenExpiryMinutes));
        resetToken.setIpAddress(ipAddress);
        resetToken.setDevice(device);
        resetToken.setBrowser(browser);
        resetToken.setUsed(false);

        passwordResetRepository.save(resetToken);
        
        emailService.sendResetPasswordEmail(user, tokenString);

        auditService.logEvent(user.getId(), "AUTH_PASSWORD_RESET_REQUESTED", "INFO", 
                "Password reset token requested and email sent. IP: " + ipAddress, user.getOrganizationId());

        return ApiResponse.<Void>builder()
                .status(200)
                .message("Password reset token generated and sent to email successfully.")
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<Void> resetPassword(ResetPasswordRequest request, String ipAddress, String device, String browser) {
        log.info("Processing reset password request using token");

        enforceRateLimit(ipAddress, "reset-password-ip");

        PasswordResetToken resetToken = passwordResetRepository.findByToken(request.getToken())
                .orElseThrow(() -> {
                    auditService.logEvent(null, "AUTH_PASSWORD_RESET_FAILED", "WARN", 
                            "Password reset attempted with invalid token: " + request.getToken(), null);
                    return new BusinessException("Invalid or expired password reset token.", "INVALID_TOKEN");
                });

        User user = resetToken.getUser();

        // Validate token expiry
        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now()) || resetToken.isDeleted()) {
            auditService.logEvent(user.getId(), "AUTH_PASSWORD_RESET_FAILED", "WARN", 
                    "Password reset attempted with expired token. IP: " + ipAddress, user.getOrganizationId());
            emailService.sendPasswordResetFailedEmail(user, "The password reset token has expired.", ipAddress, device);
            throw new BusinessException("Password reset token has expired.", "EXPIRED_TOKEN");
        }

        // Validate token not already used
        if (resetToken.isUsed()) {
            auditService.logEvent(user.getId(), "AUTH_PASSWORD_RESET_FAILED", "WARN", 
                    "Password reset attempted with already used token (Replay attack warning!). IP: " + ipAddress, user.getOrganizationId());
            emailService.sendPasswordResetFailedEmail(user, "This token has already been used. Potential replay attack.", ipAddress, device);
            throw new BusinessException("Password reset token has already been used.", "INVALID_TOKEN");
        }

        // Validate passwords matching
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            auditService.logEvent(user.getId(), "AUTH_PASSWORD_RESET_FAILED", "WARN", 
                    "Password reset failed: confirmation password does not match.", user.getOrganizationId());
            throw new BusinessException("New password and confirmation password do not match.", "PASSWORDS_DO_NOT_MATCH");
        }

        // Validate complexity & history rules
        try {
            passwordPolicyService.validateComplexity(request.getNewPassword());
            passwordPolicyService.checkHistory(user, request.getNewPassword());
        } catch (BusinessException e) {
            log.warn("Password policy check failed for user: {}", user.getEmail());
            auditService.logEvent(user.getId(), "AUTH_PASSWORD_RESET_FAILED", "WARN", 
                    "Password policy check failed: " + e.getMessage(), user.getOrganizationId());
            emailService.sendPasswordResetFailedEmail(user, e.getMessage(), ipAddress, device);
            throw e;
        }

        // Hash and save new password
        String newPasswordHash = passwordEncoder.encode(request.getNewPassword());
        user.setPasswordHash(newPasswordHash);
        userRepository.save(user);

        // Consume token
        resetToken.setUsed(true);
        resetToken.setDeleted(true);
        passwordResetRepository.save(resetToken);

        // Record history hash
        passwordPolicyService.trackPasswordChange(user, newPasswordHash);

        // Invalidate active login sessions (clear cache context & Redis sessions)
        redisTokenService.deleteAllSessions(user.getId());

        // Revoke active refresh tokens
        refreshTokenService.revokeAllTokens(user.getId());

        // Audit Event Logging
        auditService.logEvent(user.getId(), "AUTH_PASSWORD_RESET_SUCCESS", "WARN", 
                "Password reset completed. All active sessions invalidated.", user.getOrganizationId());

        // Send confirmation email
        emailService.sendPasswordChangedEmail(user);

        return ApiResponse.<Void>builder()
                .status(200)
                .message("Password updated successfully. Active sessions revoked.")
                .build();
    }
}
