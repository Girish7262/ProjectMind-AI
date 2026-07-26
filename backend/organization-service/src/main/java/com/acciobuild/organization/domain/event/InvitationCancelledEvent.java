package com.acciobuild.organization.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when an invitation is cancelled / revoked.
 */
@Getter
public class InvitationCancelledEvent extends OrganizationDomainEvent {

    private final String email;

    /**
     * Constructs the event.
     */
    public InvitationCancelledEvent(UUID tenantId, String email, String correlationId) {
        super("INVITATION_CANCELLED", tenantId, correlationId);
        this.email = email;
    }
}
