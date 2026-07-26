package com.acciobuild.project.exception;

import com.acciobuild.common.exception.BusinessException;

/**
 * Exception thrown when trying to perform operations on a project that is already soft-deleted.
 */
public class ProjectAlreadyDeletedException extends BusinessException {

    /**
     * Constructs the exception.
     */
    public ProjectAlreadyDeletedException(String message) {
        super(message, "PROJECT_ALREADY_DELETED");
    }
}
