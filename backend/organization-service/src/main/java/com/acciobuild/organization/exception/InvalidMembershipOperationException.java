package com.acciobuild.organization.exception;

import com.acciobuild.common.exception.BusinessException;

/**
 * Exception thrown when violating membership management business rules.
 */
public class InvalidMembershipOperationException extends BusinessException {

    /**
     * Constructs the exception.
     */
    public InvalidMembershipOperationException(String message) {
        super(message, "INVALID_MEMBERSHIP_OPERATION");
    }
}
