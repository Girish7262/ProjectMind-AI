package com.acciobuild.auth.security.jwt;

import com.acciobuild.auth.entity.User;
import com.acciobuild.common.constant.SecurityConstants;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Claims compiler formatting standardized claims fields into token payloads maps.
 */
@Component
public class JwtClaimsFactory {

    /**
     * Compiles user details parameters, organization IDs, and permissions list.
     */
    public Map<String, Object> createAccessClaims(User user) {
        Map<String, Object> claims = new HashMap<>();
        
        claims.put(SecurityConstants.CLAIM_USER_ID, user.getId().toString());
        claims.put(SecurityConstants.CLAIM_EMAIL, user.getEmail());
        claims.put(SecurityConstants.CLAIM_ORGANIZATION_ID, user.getOrganizationId().toString());
        claims.put("username", user.getUsername());
        claims.put("tokenType", TokenType.ACCESS.name());

        // Map Roles
        claims.put(SecurityConstants.CLAIM_ROLES, user.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toList()));

        // Map Flat Permissions
        claims.put("permissions", user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(permission -> permission.getName())
                .distinct()
                .collect(Collectors.toList()));

        return claims;
    }
}
