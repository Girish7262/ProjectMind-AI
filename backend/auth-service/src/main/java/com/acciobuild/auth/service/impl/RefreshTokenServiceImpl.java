package com.acciobuild.auth.service.impl;

import com.acciobuild.auth.entity.RefreshToken;
import com.acciobuild.auth.entity.User;
import com.acciobuild.auth.repository.RefreshTokenRepository;
import com.acciobuild.auth.service.RefreshTokenService;
import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.common.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service implementation managing refresh token validations, rotations, and revocations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository tokenRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<RefreshToken> createToken(User user, String ipAddress, String device) {
        log.info("Generating refresh token for user: {}", user.getEmail());
        
        RefreshToken token = new RefreshToken();
        token.setToken(UUID.randomUUID().toString());
        token.setExpiresAt(LocalDateTime.now().plusDays(7));
        token.setUser(user);
        token.setIpAddress(ipAddress);
        token.setDevice(device);

        RefreshToken savedToken = tokenRepository.save(token);
        return ApiResponse.<RefreshToken>builder()
                .status(200)
                .message("Token created successfully.")
                .data(savedToken)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<RefreshToken> validateToken(String tokenString) {
        log.info("Validating refresh token");
        RefreshToken token = tokenRepository.findByToken(tokenString)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token.", "INVALID_TOKEN"));

        if (token.isRevoked() || token.getExpiresAt().isBefore(LocalDateTime.now()) || token.isDeleted()) {
            throw new UnauthorizedException("Expired or revoked refresh token.", "EXPIRED_TOKEN");
        }

        return ApiResponse.<RefreshToken>builder()
                .status(200)
                .message("Token is valid.")
                .data(token)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<Void> revokeToken(String tokenString) {
        log.info("Revoking refresh token");
        tokenRepository.revokeToken(tokenString);
        return ApiResponse.<Void>builder()
                .status(200)
                .message("Token revoked successfully.")
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<Void> revokeAllTokens(UUID userId) {
        log.info("Revoking all active refresh tokens for user: {}", userId);
        tokenRepository.revokeAllUserTokens(userId);
        return ApiResponse.<Void>builder()
                .status(200)
                .message("All user tokens revoked successfully.")
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<Void> deleteExpiredTokens() {
        log.info("Purging expired refresh tokens from database");
        tokenRepository.deleteExpiredTokens(LocalDateTime.now());
        return ApiResponse.<Void>builder()
                .status(200)
                .message("Expired tokens purged successfully.")
                .build();
    }
}
