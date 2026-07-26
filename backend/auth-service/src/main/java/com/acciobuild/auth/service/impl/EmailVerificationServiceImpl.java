package com.acciobuild.auth.service.impl;

import com.acciobuild.auth.entity.EmailVerificationToken;
import com.acciobuild.auth.entity.User;
import com.acciobuild.auth.enums.AccountStatus;
import com.acciobuild.auth.repository.EmailVerificationRepository;
import com.acciobuild.auth.repository.UserRepository;
import com.acciobuild.auth.service.AuditService;
import com.acciobuild.auth.service.EmailService;
import com.acciobuild.auth.service.EmailVerificationService;
import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.common.exception.BusinessException;
import com.acciobuild.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service implementation managing email verification tokens creation and account validation confirmations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationServiceImpl implements EmailVerificationService {

    private final EmailVerificationRepository verificationRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final AuditService auditService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<EmailVerificationToken> createVerificationToken(User user) {
        log.info("Generating verification token for user: {}", user.getEmail());
        
        // Remove existing token if present
        verificationRepository.findByUserId(user.getId()).ifPresent(t -> {
            t.setDeleted(true);
            verificationRepository.save(t);
        });

        EmailVerificationToken token = new EmailVerificationToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUser(user);
        token.setExpiresAt(LocalDateTime.now().plusHours(24)); // 24 hours expiry
        token.setResendAttempts(0);

        EmailVerificationToken savedToken = verificationRepository.save(token);
        emailService.sendVerificationEmail(user, savedToken.getToken());
        auditService.logEvent(user.getId(), "AUTH_VERIFICATION_SENT", "INFO", "Verification email sent.", user.getOrganizationId());

        return ApiResponse.<EmailVerificationToken>builder()
                .status(200)
                .message("Verification token created.")
                .data(savedToken)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<Void> verifyEmail(String tokenString) {
        log.info("Processing email verification token");
        
        EmailVerificationToken token = verificationRepository.findByToken(tokenString)
                .orElseThrow(() -> new ResourceNotFoundException("Verification token not found.", "INVALID_TOKEN"));

        if (token.isDeleted() || token.getExpiresAt().isBefore(LocalDateTime.now())) {
            auditService.logEvent(token.getUser().getId(), "AUTH_VERIFICATION_FAILED", "WARN", "Verification failed: Expired token.", token.getUser().getOrganizationId());
            throw new BusinessException("Verification token expired or invalid.", "EXPIRED_TOKEN");
        }

        User user = token.getUser();
        if (user.isEmailVerified()) {
            throw new BusinessException("Email already verified.", "ALREADY_VERIFIED");
        }

        // Activate user and set verified
        user.setEmailVerified(true);
        user.setStatus(AccountStatus.ACTIVE);
        userRepository.save(user);

        // Invalidate token
        token.setDeleted(true);
        verificationRepository.save(token);

        emailService.sendWelcomeEmail(user);
        auditService.logEvent(user.getId(), "AUTH_VERIFICATION_SUCCESS", "INFO", "Email verified and account activated.", user.getOrganizationId());

        return ApiResponse.<Void>builder()
                .status(200)
                .message("Email verified and account activated successfully.")
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<Void> resendVerification(String email) {
        log.info("Processing verification resend request for: {}", email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found.", "USER_NOT_FOUND"));

        if (user.isEmailVerified()) {
            throw new BusinessException("Email already verified.", "ALREADY_VERIFIED");
        }

        EmailVerificationToken token = verificationRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    EmailVerificationToken t = new EmailVerificationToken();
                    t.setToken(UUID.randomUUID().toString());
                    t.setUser(user);
                    t.setExpiresAt(LocalDateTime.now().plusHours(24));
                    t.setResendAttempts(0);
                    return t;
                });

        if (token.getResendAttempts() >= 3) {
            log.warn("Resend limit reached for email: {}", email);
            throw new BusinessException("Maximum resend attempts reached (Limit: 3).", "TOO_MANY_REQUESTS");
        }

        // Rotate token code and increment attempts
        token.setToken(UUID.randomUUID().toString());
        token.setExpiresAt(LocalDateTime.now().plusHours(24));
        token.setResendAttempts(token.getResendAttempts() + 1);
        token.setDeleted(false);
        verificationRepository.save(token);

        emailService.sendVerificationEmail(user, token.getToken());
        auditService.logEvent(user.getId(), "AUTH_VERIFICATION_RESEND", "INFO", "Verification email resent.", user.getOrganizationId());

        return ApiResponse.<Void>builder()
                .status(200)
                .message("Verification email resent successfully.")
                .build();
    }
}
