package com.acciobuild.project.exception;

import com.acciobuild.common.exception.BusinessException;

/**
 * Exception thrown when trying to perform operations on a project that is already archived.
 */
public class ProjectAlreadyArchivedException extends BusinessException {

    /**
     * Constructs the exception.
     */
    public ProjectAlreadyArchivedException(String message) {
        super(message, "PROJECT_ALREADY_ARCHIVED");
    }
}
