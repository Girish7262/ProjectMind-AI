package com.acciobuild.organization.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when an organization profile is soft deleted.
 */
@Getter
public class OrganizationDeletedEvent extends OrganizationDomainEvent {

    /**
     * Constructs the event.
     */
    public OrganizationDeletedEvent(UUID tenantId, String correlationId) {
        super("ORGANIZATION_DELETED", tenantId, correlationId);
    }
}
