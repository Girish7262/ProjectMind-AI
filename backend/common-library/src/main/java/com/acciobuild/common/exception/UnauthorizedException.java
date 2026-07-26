package com.acciobuild.common.exception;

/**
 * Exception thrown when requests lack valid authentication credentials.
 */
public class UnauthorizedException extends GlobalException {
    public UnauthorizedException(String message, String errorCode) {
        super(message, errorCode, 401);
    }
}
