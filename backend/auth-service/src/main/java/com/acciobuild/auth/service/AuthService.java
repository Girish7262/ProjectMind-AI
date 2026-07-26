package com.acciobuild.auth.service;

import com.acciobuild.auth.dto.*;
import com.acciobuild.common.dto.ApiResponse;
import java.util.UUID;

/**
 * Service interface outlining credentials verification, token generation, and logout operations.
 */
public interface AuthService {

    /**
     * Registers a new user within the platform organization boundaries.
     */
    ApiResponse<UserProfileResponse> register(RegisterRequest request);

    /**
     * Validates user credentials, logs attempts details, and generates access and refresh tokens.
     */
    ApiResponse<LoginResponse> login(LoginRequest request, String ipAddress, String userAgent);

    /**
     * Rotates refresh tokens and issues refreshed access tokens.
     */
    ApiResponse<LoginResponse> refresh(RefreshTokenRequest request);

    /**
     * Invalidates refresh tokens and logs out user sessions.
     */
    ApiResponse<Void> logout(LogoutRequest request);

    /**
     * Changes an authenticated user's password.
     */
    ApiResponse<Void> changePassword(UUID userId, ChangePasswordRequest request);
}
