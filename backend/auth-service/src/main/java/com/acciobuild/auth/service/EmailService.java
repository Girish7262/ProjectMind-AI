package com.acciobuild.auth.service;

import com.acciobuild.auth.entity.User;
import com.acciobuild.common.dto.ApiResponse;

/**
 * Service interface outlining automated email messaging requests.
 */
public interface EmailService {

    ApiResponse<Void> sendVerificationEmail(User user, String token);

    ApiResponse<Void> sendResetPasswordEmail(User user, String token);

    ApiResponse<Void> sendWelcomeEmail(User user);

    ApiResponse<Void> sendPasswordChangedEmail(User user);

    ApiResponse<Void> sendPasswordResetFailedEmail(User user, String reason, String ipAddress, String device);
}
