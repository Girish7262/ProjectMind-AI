package com.acciobuild.project.exception;

import com.acciobuild.common.exception.BusinessException;

/**
 * Exception thrown when enrolling a user who is already a member of the project.
 */
public class MemberAlreadyExistsException extends BusinessException {

    /**
     * Constructs the exception.
     */
    public MemberAlreadyExistsException(String message) {
        super(message, "MEMBER_ALREADY_EXISTS");
    }
}
