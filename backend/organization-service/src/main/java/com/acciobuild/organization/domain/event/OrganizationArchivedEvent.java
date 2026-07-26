package com.acciobuild.organization.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when an organization is archived.
 */
@Getter
public class OrganizationArchivedEvent extends OrganizationDomainEvent {

    /**
     * Constructs the event.
     */
    public OrganizationArchivedEvent(UUID tenantId, String correlationId) {
        super("ORGANIZATION_ARCHIVED", tenantId, correlationId);
    }
}
