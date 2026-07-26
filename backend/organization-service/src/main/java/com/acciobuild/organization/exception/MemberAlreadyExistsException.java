package com.acciobuild.organization.exception;

import com.acciobuild.common.exception.BusinessException;

/**
 * Exception thrown when a user is already enrolled as a member of the organization.
 */
public class MemberAlreadyExistsException extends BusinessException {

    /**
     * Constructs the exception.
     */
    public MemberAlreadyExistsException(String message) {
        super(message, "MEMBER_ALREADY_EXISTS");
    }
}
