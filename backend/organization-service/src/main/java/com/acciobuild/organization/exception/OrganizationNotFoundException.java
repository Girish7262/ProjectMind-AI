package com.acciobuild.organization.exception;

import com.acciobuild.common.exception.ResourceNotFoundException;

/**
 * Exception thrown when a requested organization resource is not found.
 */
public class OrganizationNotFoundException extends ResourceNotFoundException {
    
    /**
     * Constructs the exception.
     */
    public OrganizationNotFoundException(String message) {
        super(message, "ORGANIZATION_NOT_FOUND");
    }
}
