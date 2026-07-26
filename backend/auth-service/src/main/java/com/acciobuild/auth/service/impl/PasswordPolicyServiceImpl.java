package com.acciobuild.auth.service.impl;

import com.acciobuild.auth.entity.PasswordHistory;
import com.acciobuild.auth.entity.User;
import com.acciobuild.auth.repository.PasswordHistoryRepository;
import com.acciobuild.auth.service.PasswordPolicyService;
import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Service implementation enforcing password complexity and history constraints.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordPolicyServiceImpl implements PasswordPolicyService {

    private final PasswordHistoryRepository historyRepository;
    private final PasswordEncoder passwordEncoder;

    // Pattern enforces: min 12 chars, at least one uppercase letter, one lowercase letter, one digit, and one special character
    private static final String COMPLEXITY_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#\\-_])[A-Za-z\\d@$!%*?&#\\-_]{12,}$";
    private static final Pattern PATTERN = Pattern.compile(COMPLEXITY_REGEX);

    @Override
    public ApiResponse<Void> validateComplexity(String password) {
        log.info("Validating password complexity");
        
        if (password == null || password.trim().isEmpty()) {
            throw new BusinessException("Password cannot be empty.", "PASSWORD_REQUIRED");
        }

        if (!PATTERN.matcher(password).matches()) {
            throw new BusinessException(
                    "Password does not meet complexity requirements: Minimum 12 characters, at least one uppercase letter, one lowercase letter, one numeric digit, and one special character (@$!%*?&#-_).",
                    "PASSWORD_TOO_WEAK"
            );
        }

        return ApiResponse.<Void>builder()
                .status(200)
                .message("Password complexity validated successfully.")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<Void> checkHistory(User user, String newPassword) {
        log.info("Checking password history for user: {}", user.getEmail());

        // 1. Reject matching the current active password
        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            throw new BusinessException("New password cannot be the same as the current password.", "PASSWORD_SAME_AS_CURRENT");
        }

        // 2. Reject matching any of the previous 5 passwords
        List<PasswordHistory> history = historyRepository.findRecentPasswords(user.getId(), PageRequest.of(0, 5));
        for (PasswordHistory record : history) {
            if (passwordEncoder.matches(newPassword, record.getPasswordHash())) {
                throw new BusinessException("New password cannot be one of the last 5 previously used passwords.", "PASSWORD_REUSED");
            }
        }

        return ApiResponse.<Void>builder()
                .status(200)
                .message("Password history validation passed.")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<Boolean> isPasswordExpired(User user) {
        log.info("Checking if password has expired for user: {}", user.getEmail());
        
        // Check if latest history entry or user creation is older than 90 days (default policy)
        List<PasswordHistory> history = historyRepository.findRecentPasswords(user.getId(), PageRequest.of(0, 1));
        LocalDateTime referenceTime = history.isEmpty() ? user.getCreatedAt() : history.get(0).getCreatedAt();

        if (referenceTime == null) {
            referenceTime = LocalDateTime.now();
        }

        boolean expired = referenceTime.plusDays(90).isBefore(LocalDateTime.now());
        
        return ApiResponse.<Boolean>builder()
                .status(200)
                .message(expired ? "Password has expired." : "Password is active.")
                .data(expired)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<Void> trackPasswordChange(User user, String encodedPassword) {
        log.info("Tracking password change history entry for user: {}", user.getEmail());
        
        PasswordHistory record = new PasswordHistory();
        record.setUser(user);
        record.setPasswordHash(encodedPassword);
        historyRepository.save(record);

        // Optional: Prune entries beyond the last 10 to manage database growth
        List<PasswordHistory> history = historyRepository.findRecentPasswords(user.getId(), PageRequest.of(10, 20));
        if (!history.isEmpty()) {
            historyRepository.deleteAll(history);
        }

        return ApiResponse.<Void>builder()
                .status(200)
                .message("Password history updated successfully.")
                .build();
    }
}
