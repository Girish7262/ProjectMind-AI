package com.acciobuild.project.exception;

import com.acciobuild.common.exception.BusinessException;

/**
 * Exception thrown when modifying an archived read-only project.
 */
public class ProjectArchivedException extends BusinessException {

    /**
     * Constructs the exception.
     */
    public ProjectArchivedException(String message) {
        super(message, "PROJECT_ARCHIVED");
    }
}
