package com.acciobuild.organization.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when an invitation is generated for a user email.
 */
@Getter
public class InvitationSentEvent extends OrganizationDomainEvent {

    private final String email;
    private final UUID invitedBy;

    /**
     * Constructs the event.
     */
    public InvitationSentEvent(UUID tenantId, String email, UUID invitedBy, String correlationId) {
        super("INVITATION_SENT", tenantId, correlationId);
        this.email = email;
        this.invitedBy = invitedBy;
    }
}
