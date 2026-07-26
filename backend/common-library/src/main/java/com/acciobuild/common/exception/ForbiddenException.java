package com.acciobuild.common.exception;

/**
 * Exception thrown when authenticated users lack permissions to resource actions.
 */
public class ForbiddenException extends GlobalException {
    public ForbiddenException(String message, String errorCode) {
        super(message, errorCode, 403);
    }
}
