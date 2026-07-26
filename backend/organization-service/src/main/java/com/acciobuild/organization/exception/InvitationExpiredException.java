package com.acciobuild.organization.exception;

import com.acciobuild.common.exception.BusinessException;

/**
 * Exception thrown when attempting to accept or process an expired organization invitation.
 */
public class InvitationExpiredException extends BusinessException {

    /**
     * Constructs the exception.
     */
    public InvitationExpiredException(String message) {
        super(message, "INVITATION_EXPIRED");
    }
}
