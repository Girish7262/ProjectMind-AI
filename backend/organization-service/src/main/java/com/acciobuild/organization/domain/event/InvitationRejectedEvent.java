package com.acciobuild.organization.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when an invitation is explicitly rejected.
 */
@Getter
public class InvitationRejectedEvent extends OrganizationDomainEvent {

    private final String email;

    /**
     * Constructs the event.
     */
    public InvitationRejectedEvent(UUID tenantId, String email, String correlationId) {
        super("INVITATION_REJECTED", tenantId, correlationId);
        this.email = email;
    }
}
