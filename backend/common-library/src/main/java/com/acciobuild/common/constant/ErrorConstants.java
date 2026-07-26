package com.acciobuild.common.constant;

/**
 * Standard system error code strings.
 */
public final class ErrorConstants {
    private ErrorConstants() {}

    public static final String ERR_RESOURCE_NOT_FOUND = "RESOURCE_NOT_FOUND";
    public static final String ERR_UNAUTHORIZED = "UNAUTHORIZED_ACCESS";
    public static final String ERR_FORBIDDEN = "FORBIDDEN_ACTION";
    public static final String ERR_CONFLICT = "RESOURCE_CONFLICT";
    public static final String ERR_VALIDATION = "INPUT_VALIDATION_ERROR";
    public static final String ERR_INTERNAL_SERVER = "INTERNAL_SERVER_ERROR";
    public static final String ERR_BUSINESS_RULE = "BUSINESS_RULE_VIOLATION";
}
