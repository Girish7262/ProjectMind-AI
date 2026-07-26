package com.acciobuild.auth.service;

import com.acciobuild.auth.dto.ForgotPasswordRequest;
import com.acciobuild.auth.dto.ResetPasswordRequest;
import com.acciobuild.common.dto.ApiResponse;

/**
 * Service interface managing password recovery requests and token resets.
 */
public interface PasswordResetService {

    /**
     * Initiates password recovery process by generating a unique reset token and sending an email to the user.
     */
    ApiResponse<Void> forgotPassword(ForgotPasswordRequest request, String ipAddress, String device, String browser);

    /**
     * Completes password reset process by verifying the token, validating password policies, and updating the user's password.
     */
    ApiResponse<Void> resetPassword(ResetPasswordRequest request, String ipAddress, String device, String browser);
}
