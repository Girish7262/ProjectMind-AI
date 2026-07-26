package com.acciobuild.auth.service;

import com.acciobuild.auth.entity.EmailVerificationToken;
import com.acciobuild.auth.entity.User;
import com.acciobuild.common.dto.ApiResponse;

/**
 * Service interface managing email verification tokens creation and account validation confirmations.
 */
public interface EmailVerificationService {

    ApiResponse<EmailVerificationToken> createVerificationToken(User user);

    ApiResponse<Void> verifyEmail(String tokenString);

    ApiResponse<Void> resendVerification(String email);
}
