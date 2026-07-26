package com.acciobuild.project.exception;

import com.acciobuild.common.exception.BusinessException;

/**
 * Exception thrown when a project name or code collision is detected.
 */
public class DuplicateProjectException extends BusinessException {

    /**
     * Constructs the exception.
     */
    public DuplicateProjectException(String message) {
        super(message, "DUPLICATE_PROJECT");
    }
}
