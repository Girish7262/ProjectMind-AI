package com.acciobuild.common.constant;

/**
 * Standard HTTP header name constants used across microservices.
 */
public final class HeaderConstants {
    private HeaderConstants() {}

    public static final String CORRELATION_ID = "X-Correlation-Id";
    public static final String TENANT_ID = "X-Tenant-Id";
    public static final String USER_ID = "X-User-Id";
}
