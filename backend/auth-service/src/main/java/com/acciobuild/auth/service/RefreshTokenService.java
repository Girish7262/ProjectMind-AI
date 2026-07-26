package com.acciobuild.auth.service;

import com.acciobuild.auth.entity.RefreshToken;
import com.acciobuild.auth.entity.User;
import com.acciobuild.common.dto.ApiResponse;
import java.util.UUID;

/**
 * Service interface managing refresh tokens lifecycle rules.
 */
public interface RefreshTokenService {

    ApiResponse<RefreshToken> createToken(User user, String ipAddress, String device);

    ApiResponse<RefreshToken> validateToken(String tokenString);

    ApiResponse<Void> revokeToken(String tokenString);

    ApiResponse<Void> revokeAllTokens(UUID userId);

    ApiResponse<Void> deleteExpiredTokens();
}
