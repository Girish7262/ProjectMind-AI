package com.acciobuild.auth.security.jwt;

import com.acciobuild.auth.entity.User;
import com.acciobuild.common.constant.SecurityConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Token manager orchestrating JWT parsing, key signatures validations (HS512), and claims extraction.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {

    private final JwtProperties properties;
    private final JwtClaimsFactory claimsFactory;

    private io.jsonwebtoken.security.Keys Keys; // Helper ref

    private javax.crypto.SecretKey getSigningKey() {
        byte[] keyBytes = properties.getSecret().getBytes(StandardCharsets.UTF_8);
        return io.jsonwebtoken.security.Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generates a signed JWT access token for a User.
     */
    public String generateAccessToken(User user) {
        Map<String, Object> claims = claimsFactory.createAccessClaims(user);
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + properties.getAccessTokenExpiration());

        return Jwts.builder()
                .claims(claims)
                .subject(user.getEmail())
                .issuer(properties.getIssuer())
                .audience().add(properties.getAudience()).and()
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey(), Jwts.SIG.HS512)
                .compact();
    }

    /**
     * Parses and returns claims from a token string.
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .clockSkewSeconds(properties.getClockSkew())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Validates whether a token signature and structure is correct and active.
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            log.error("JWT Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public String extractUsername(String token) {
        return parseToken(token).getSubject();
    }

    public UUID extractUserId(String token) {
        String userIdStr = parseToken(token).get(SecurityConstants.CLAIM_USER_ID, String.class);
        return UUID.fromString(userIdStr);
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        return parseToken(token).get(SecurityConstants.CLAIM_ROLES, List.class);
    }

    @SuppressWarnings("unchecked")
    public List<String> extractPermissions(String token) {
        return parseToken(token).get("permissions", List.class);
    }

    public UUID extractOrganization(String token) {
        String orgIdStr = parseToken(token).get(SecurityConstants.CLAIM_ORGANIZATION_ID, String.class);
        return UUID.fromString(orgIdStr);
    }

    public Date getExpiration(String token) {
        return parseToken(token).getExpiration();
    }

    public boolean isExpired(String token) {
        return getExpiration(token).before(new Date());
    }
}
