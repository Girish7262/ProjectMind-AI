package com.acciobuild.common.exception;

/**
 * Exception thrown when requested resources are not found.
 */
public class ResourceNotFoundException extends GlobalException {
    public ResourceNotFoundException(String message, String errorCode) {
        super(message, errorCode, 404);
    }
}
