package com.acciobuild.common.exception;

/**
 * Exception representing general business rule validation violations.
 */
public class BusinessException extends GlobalException {
    public BusinessException(String message, String errorCode) {
        super(message, errorCode, 400);
    }
}
