package com.acciobuild.project.exception;

import com.acciobuild.common.exception.BusinessException;

/**
 * Exception thrown when a requested project operation violates business rules.
 */
public class InvalidProjectOperationException extends BusinessException {

    /**
     * Constructs the exception.
     */
    public InvalidProjectOperationException(String message) {
        super(message, "INVALID_PROJECT_OPERATION");
    }
}
