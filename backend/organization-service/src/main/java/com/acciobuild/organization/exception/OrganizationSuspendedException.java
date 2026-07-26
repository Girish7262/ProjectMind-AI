package com.acciobuild.organization.exception;

import com.acciobuild.common.exception.BusinessException;

/**
 * Exception thrown when attempting to perform operations on a suspended organization.
 */
public class OrganizationSuspendedException extends BusinessException {

    /**
     * Constructs the exception.
     */
    public OrganizationSuspendedException(String message) {
        super(message, "ORGANIZATION_SUSPENDED");
    }
}
