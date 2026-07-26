package com.acciobuild.common.exception;

/**
 * Exception thrown when validation fails.
 */
public class ValidationException extends GlobalException {
    public ValidationException(String message, String errorCode) {
        super(message, errorCode, 400);
    }
}
