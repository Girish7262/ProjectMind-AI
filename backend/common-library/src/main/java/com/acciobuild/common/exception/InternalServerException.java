package com.acciobuild.common.exception;

/**
 * Exception representing unexpected internal system failures.
 */
public class InternalServerException extends GlobalException {
    public InternalServerException(String message, String errorCode) {
        super(message, errorCode, 500);
    }
}
