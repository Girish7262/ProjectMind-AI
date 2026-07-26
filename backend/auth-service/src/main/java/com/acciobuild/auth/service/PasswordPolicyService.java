package com.acciobuild.auth.service;

import com.acciobuild.auth.entity.User;
import com.acciobuild.common.dto.ApiResponse;

/**
 * Service interface enforcing credentials security policies.
 */
public interface PasswordPolicyService {

    ApiResponse<Void> validateComplexity(String password);

    ApiResponse<Void> checkHistory(User user, String newPassword);

    ApiResponse<Boolean> isPasswordExpired(User user);

    ApiResponse<Void> trackPasswordChange(User user, String encodedPassword);
}
