package com.acciobuild.auth.security.jwt;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Set;

/**
 * Audit helper checking token issues matching yml configurations metadata.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtValidator {

    private final JwtTokenProvider tokenProvider;
    private final JwtProperties properties;

    /**
     * Checks if token matches active configurations specifications.
     */
    public boolean isValid(String token) {
        try {
            if (!tokenProvider.validateToken(token)) {
                return false;
            }
            
            Claims claims = tokenProvider.parseToken(token);
            
            // Validate Issuer
            if (!properties.getIssuer().equals(claims.getIssuer())) {
                log.warn("JWT validation failed: Invalid issuer");
                return false;
            }

            // Validate Audience
            Set<String> audience = claims.getAudience();
            if (audience == null || !audience.contains(properties.getAudience())) {
                log.warn("JWT validation failed: Invalid audience");
                return false;
            }

            // Validate Token Type
            String tokenType = claims.get("tokenType", String.class);
            if (!TokenType.ACCESS.name().equals(tokenType)) {
                log.warn("JWT validation failed: Token type mismatch");
                return false;
            }

            return true;
        } catch (Exception e) {
            log.error("JWT validation error: {}", e.getMessage());
            return false;
        }
    }
}
