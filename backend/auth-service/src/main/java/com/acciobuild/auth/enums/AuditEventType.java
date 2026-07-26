package com.acciobuild.auth.enums;

/**
 * Enumeration defining security-sensitive system event types.
 */
public enum AuditEventType {
    LOGIN_SUCCESS,
    LOGIN_FAILED,
    LOGOUT,
    REGISTER,
    EMAIL_VERIFIED,
    PASSWORD_CHANGED,
    PASSWORD_RESET,
    ROLE_ASSIGNED,
    ROLE_REMOVED,
    PERMISSION_ASSIGNED,
    PERMISSION_REMOVED,
    TOKEN_REFRESH,
    ACCOUNT_LOCKED,
    ACCOUNT_UNLOCKED,
    ACCESS_DENIED,
    UNAUTHORIZED,
    PROFILE_UPDATED
}
