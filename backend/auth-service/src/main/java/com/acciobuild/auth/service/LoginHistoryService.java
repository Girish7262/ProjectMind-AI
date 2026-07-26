package com.acciobuild.auth.service;

import com.acciobuild.auth.entity.LoginHistory;
import com.acciobuild.common.dto.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

/**
 * Service interface auditing logins/logouts statistics.
 */
public interface LoginHistoryService {

    ApiResponse<Void> saveLogin(UUID userId, String ipAddress, String device, String browser, String os, String country, String city);

    ApiResponse<Void> saveLogout(UUID userId);

    ApiResponse<Void> failedLogin(String email, String ipAddress, String device, String browser, String os, String failureReason);

    ApiResponse<Page<LoginHistory>> getHistory(UUID userId, Pageable pageable);
}
