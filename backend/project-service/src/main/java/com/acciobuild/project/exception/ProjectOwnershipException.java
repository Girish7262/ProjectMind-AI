package com.acciobuild.project.exception;

import com.acciobuild.common.exception.BusinessException;

/**
 * Exception thrown when project ownership transfer rules are violated.
 */
public class ProjectOwnershipException extends BusinessException {

    /**
     * Constructs the exception.
     */
    public ProjectOwnershipException(String message) {
        super(message, "PROJECT_OWNERSHIP_ERROR");
    }
}
