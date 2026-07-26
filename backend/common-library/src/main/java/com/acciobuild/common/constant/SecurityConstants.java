package com.acciobuild.common.constant;

/**
 * Common security and JWT configuration constants.
 */
public final class SecurityConstants {
    private SecurityConstants() {}

    public static final String JWT_HEADER = "Authorization";
    public static final String JWT_TOKEN_PREFIX = "Bearer ";
    
    // Claim Keys
    public static final String CLAIM_ORGANIZATION_ID = "org_id";
    public static final String CLAIM_ROLES = "roles";
    public static final String CLAIM_USER_ID = "user_id";
    public static final String CLAIM_EMAIL = "email";
    
    // Roles Definitions
    public static final String ROLE_PREFIX = "ROLE_";
    public static final String ROLE_OWNER = "OWNER";
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_PROJECT_MANAGER = "PROJECT_MANAGER";
    public static final String ROLE_DEVELOPER = "DEVELOPER";
    public static final String ROLE_VIEWER = "VIEWER";
}
