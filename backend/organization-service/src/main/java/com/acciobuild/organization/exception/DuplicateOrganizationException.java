package com.acciobuild.organization.exception;

import com.acciobuild.common.exception.BusinessException;

/**
 * Exception thrown when attempting to create an organization with a code or name that already exists.
 */
public class DuplicateOrganizationException extends BusinessException {

    /**
     * Constructs the exception.
     */
    public DuplicateOrganizationException(String message) {
        super(message, "DUPLICATE_ORGANIZATION");
    }
}
