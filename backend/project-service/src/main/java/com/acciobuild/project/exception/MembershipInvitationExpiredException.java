package com.acciobuild.project.exception;

import com.acciobuild.common.exception.BusinessException;

/**
 * Exception thrown when trying to accept an expired project membership invitation.
 */
public class MembershipInvitationExpiredException extends BusinessException {

    /**
     * Constructs the exception.
     */
    public MembershipInvitationExpiredException(String message) {
        super(message, "INVITATION_EXPIRED");
    }
}
