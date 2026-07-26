package com.acciobuild.auth.service;

import com.acciobuild.auth.entity.RefreshToken;
import com.acciobuild.auth.entity.User;
import java.util.UUID;

/**
 * Service interface outlining JWT access tokens and UUID refresh tokens lifecycle rules.
 */
public interface TokenService {

    /**
     * Generates a signed access token (JWT) for a user.
     */
    String generateAccessToken(User user);

    /**
     * Generates a refresh token (UUID) for a user session.
     */
    RefreshToken generateRefreshToken(User user);

    /**
     * Verifies refresh token state (revoked status, expiration times).
     */
    RefreshToken verifyRefreshToken(String tokenString);

    /**
     * Invalidates all refresh tokens for a user.
     */
    void revokeTokensForUser(UUID userId);
}
