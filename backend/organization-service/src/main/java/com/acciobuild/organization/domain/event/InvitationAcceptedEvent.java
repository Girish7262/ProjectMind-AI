package com.acciobuild.organization.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when an invitation is accepted.
 */
@Getter
public class InvitationAcceptedEvent extends OrganizationDomainEvent {

    private final String email;
    private final UUID userId;

    /**
     * Constructs the event.
     */
    public InvitationAcceptedEvent(UUID tenantId, String email, UUID userId, String correlationId) {
        super("INVITATION_ACCEPTED", tenantId, correlationId);
        this.email = email;
        this.userId = userId;
    }
}
