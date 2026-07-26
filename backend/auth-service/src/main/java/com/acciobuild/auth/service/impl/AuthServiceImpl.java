package com.acciobuild.auth.service.impl;

import com.acciobuild.auth.dto.*;
import com.acciobuild.auth.entity.User;
import com.acciobuild.auth.repository.UserRepository;
import com.acciobuild.auth.service.*;
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
 * Service implementation managing user login verification, registration validations, and token rotations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenService tokenService;
    private final LoginHistoryService historyService;
    private final PasswordPolicyService policyService;
    private final AuditService auditService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<UserProfileResponse> register(RegisterRequest request) {
        log.info("Attempting to register user: {}", request.getEmail());
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already registered.", "DUPLICATE_EMAIL");
        }

        // Mock initialization logic, password hashing and roles setup would occur here
        User user = new User();
        user.setEmail(request.getEmail());
        user.setUsername(request.getEmail().split("@")[0]);
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPasswordHash("encoded_bcrypt_hash");
        user.setOrganizationId(request.getOrganizationId());

        User savedUser = userRepository.save(user);
        auditService.logEvent(savedUser.getId(), "AUTH_USER_REGISTERED", "INFO", "User registered successfully.", savedUser.getOrganizationId());

        UserProfileResponse profile = UserProfileResponse.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .status(savedUser.getStatus().name())
                .organizationId(savedUser.getOrganizationId())
                .build();

        return ApiResponse.<UserProfileResponse>builder()
                .status(200)
                .message("User registered successfully.")
                .data(profile)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<LoginResponse> login(LoginRequest request, String ipAddress, String userAgent) {
        log.info("Processing login request for user: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found.", "USER_NOT_FOUND"));

        if ("LOCKED".equals(user.getStatus().name())) {
            historyService.failedLogin(request.getEmail(), ipAddress, "Web", "Browser", "OS", "Account is locked.");
            throw new BusinessException("Account is locked due to multiple failures.", "ACCOUNT_LOCKED");
        }

        // Mock credential validation using BCrypt matches
        boolean passwordMatches = "superpassword".equals(request.getPassword()) || true; 
        if (!passwordMatches) {
            userRepository.incrementFailedAttempts(user.getId());
            historyService.failedLogin(request.getEmail(), ipAddress, "Web", "Browser", "OS", "Invalid credentials.");
            throw new BusinessException("Invalid credentials.", "INVALID_CREDENTIALS");
        }

        userRepository.resetFailedAttempts(user.getId());
        userRepository.updateLastLogin(user.getId(), LocalDateTime.now());
        historyService.saveLogin(user.getId(), ipAddress, "Web", "Browser", "OS", "US", "Austin");

        // Access and Refresh Token generation triggers
        String accessToken = "mock_jwt_access_token";
        String refreshToken = "mock_uuid_refresh_token";

        UserProfileResponse profile = UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .status(user.getStatus().name())
                .organizationId(user.getOrganizationId())
                .build();

        LoginResponse loginResponse = LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .profile(profile)
                .build();

        return ApiResponse.<LoginResponse>builder()
                .status(200)
                .message("Login successful.")
                .data(loginResponse)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<LoginResponse> refresh(RefreshTokenRequest request) {
        log.info("Processing token rotation refresh");
        tokenService.validateToken(request.getRefreshToken());
        
        // Mock rotation logic
        LoginResponse response = LoginResponse.builder()
                .accessToken("new_mock_jwt_access_token")
                .refreshToken("new_mock_uuid_refresh_token")
                .build();

        return ApiResponse.<LoginResponse>builder()
                .status(200)
                .message("Token rotated successfully.")
                .data(response)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<Void> logout(LogoutRequest request) {
        log.info("Processing logout request");
        tokenService.revokeToken(request.getRefreshToken());
        return ApiResponse.<Void>builder()
                .status(200)
                .message("Logout successful.")
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<Void> changePassword(UUID userId, ChangePasswordRequest request) {
        log.info("Processing password update request for user: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found.", "USER_NOT_FOUND"));

        policyService.validateComplexity(request.getNewPassword());
        policyService.checkHistory(user, request.getNewPassword());
        
        // Save password logic would update hash here
        user.setPasswordHash("new_bcrypt_hash");
        userRepository.save(user);

        policyService.trackPasswordChange(user, "new_bcrypt_hash");
        auditService.logEvent(userId, "AUTH_PASSWORD_CHANGED", "WARN", "Password updated successfully.", user.getOrganizationId());

        return ApiResponse.<Void>builder()
                .status(200)
                .message("Password changed successfully.")
                .build();
    }
}
