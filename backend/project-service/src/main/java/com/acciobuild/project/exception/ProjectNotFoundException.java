package com.acciobuild.project.exception;

import com.acciobuild.common.exception.ResourceNotFoundException;

/**
 * Exception thrown when a requested project is not found.
 */
public class ProjectNotFoundException extends ResourceNotFoundException {

    /**
     * Constructs the exception.
     */
    public ProjectNotFoundException(String message) {
        super(message, "PROJECT_NOT_FOUND");
    }
}
