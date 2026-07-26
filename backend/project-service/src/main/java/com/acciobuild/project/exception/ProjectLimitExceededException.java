package com.acciobuild.project.exception;

import com.acciobuild.common.exception.BusinessException;

/**
 * Exception thrown when a tenant's project quotas limits are exceeded.
 */
public class ProjectLimitExceededException extends BusinessException {

    /**
     * Constructs the exception.
     */
    public ProjectLimitExceededException(String message) {
        super(message, "PROJECT_LIMIT_EXCEEDED");
    }
}
