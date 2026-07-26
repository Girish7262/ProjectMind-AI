package com.acciobuild.organization.service;

import java.time.LocalDateTime;

/**
 * Service interface managing email dispatch notifications.
 */
public interface EmailService {

    /**
     * Sends an HTML organization invitation email asynchronously.
     */
    void sendInvitationEmail(String recipientEmail, String organizationName, String inviterName, String invitationLink, LocalDateTime expiresAt);
}
