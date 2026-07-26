package com.acciobuild.auth.service.impl;

import com.acciobuild.auth.entity.LoginHistory;
import com.acciobuild.auth.entity.User;
import com.acciobuild.auth.repository.LoginHistoryRepository;
import com.acciobuild.auth.repository.UserRepository;
import com.acciobuild.auth.service.LoginHistoryService;
import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service implementation auditing logins and logouts history statistics.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoginHistoryServiceImpl implements LoginHistoryService {

    private final LoginHistoryRepository loginHistoryRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<Void> saveLogin(UUID userId, String ipAddress, String device, String browser, String os, String country, String city) {
        log.info("Saving successful login log for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));
        
        LoginHistory history = new LoginHistory();
        history.setUser(user);
        history.setIpAddress(ipAddress);
        history.setDevice(device);
        history.setBrowser(browser);
        history.setOperatingSystem(os);
        history.setCountry(country);
        history.setCity(city);
        history.setSuccess(true);
        history.setLoginTime(LocalDateTime.now());
        
        loginHistoryRepository.save(history);
        return ApiResponse.<Void>builder()
                .status(200)
                .message("Login trace recorded successfully")
                .data(null)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<Void> saveLogout(UUID userId) {
        log.info("Saving logout time for user ID: {}", userId);
        
        // Find most recent successful login entry that has not logged out yet
        Pageable topOne = org.springframework.data.domain.PageRequest.of(0, 1);
        java.util.List<LoginHistory> recent = loginHistoryRepository.findSuccessfulLogins(userId, topOne);
        
        if (!recent.isEmpty()) {
            LoginHistory latest = recent.get(0);
            if (latest.getLogoutTime() == null) {
                latest.setLogoutTime(LocalDateTime.now());
                loginHistoryRepository.save(latest);
            }
        }
        
        return ApiResponse.<Void>builder()
                .status(200)
                .message("Logout trace recorded successfully")
                .data(null)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<Void> failedLogin(String email, String ipAddress, String device, String browser, String os, String failureReason) {
        log.warn("Saving failed login attempt for email: {}. Reason: {}", email, failureReason);
        
        userRepository.findByEmail(email).ifPresent(user -> {
            LoginHistory history = new LoginHistory();
            history.setUser(user);
            history.setIpAddress(ipAddress);
            history.setDevice(device);
            history.setBrowser(browser);
            history.setOperatingSystem(os);
            history.setSuccess(false);
            history.setFailureReason(failureReason);
            history.setLoginTime(LocalDateTime.now());
            
            loginHistoryRepository.save(history);
        });
        
        return ApiResponse.<Void>builder()
                .status(200)
                .message("Failed login attempt recorded successfully")
                .data(null)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<Page<LoginHistory>> getHistory(UUID userId, Pageable pageable) {
        log.info("Fetching login history page for user ID: {}", userId);
        Page<LoginHistory> historyPage = loginHistoryRepository.findByUserId(userId, pageable);
        return ApiResponse.<Page<LoginHistory>>builder()
                .status(200)
                .message("History page retrieved successfully")
                .data(historyPage)
                .build();
    }
}
