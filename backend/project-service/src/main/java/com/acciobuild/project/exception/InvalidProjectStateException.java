package com.acciobuild.project.exception;

import com.acciobuild.common.exception.BusinessException;

/**
 * Exception thrown when trying to perform an invalid state transition on a project.
 */
public class InvalidProjectStateException extends BusinessException {

    /**
     * Constructs the exception.
     */
    public InvalidProjectStateException(String message) {
        super(message, "INVALID_PROJECT_STATE");
    }
}
