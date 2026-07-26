package com.acciobuild.common.exception;

/**
 * Exception thrown on resource state conflicts, such as unique key violations.
 */
public class ConflictException extends GlobalException {
    public ConflictException(String message, String errorCode) {
        super(message, errorCode, 409);
    }
}
