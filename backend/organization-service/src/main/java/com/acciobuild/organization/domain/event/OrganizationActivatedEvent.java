package com.acciobuild.organization.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when an organization state changes to ACTIVE.
 */
@Getter
public class OrganizationActivatedEvent extends OrganizationDomainEvent {

    /**
     * Constructs the event.
     */
    public OrganizationActivatedEvent(UUID tenantId, String correlationId) {
        super("ORGANIZATION_ACTIVATED", tenantId, correlationId);
    }
}
