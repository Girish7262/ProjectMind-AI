package com.acciobuild.organization.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when an invitation expires.
 */
@Getter
public class InvitationExpiredEvent extends OrganizationDomainEvent {

    private final String email;

    /**
     * Constructs the event.
     */
    public InvitationExpiredEvent(UUID tenantId, String email, String correlationId) {
        super("INVITATION_EXPIRED", tenantId, correlationId);
        this.email = email;
    }
}
