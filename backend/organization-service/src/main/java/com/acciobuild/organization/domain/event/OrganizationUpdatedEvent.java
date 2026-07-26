package com.acciobuild.organization.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when an organization profile metadata changes.
 */
@Getter
public class OrganizationUpdatedEvent extends OrganizationDomainEvent {

    private final String organizationName;
    private final String status;

    /**
     * Constructs the event.
     */
    public OrganizationUpdatedEvent(UUID tenantId, String name, String status, String correlationId) {
        super("ORGANIZATION_UPDATED", tenantId, correlationId);
        this.organizationName = name;
        this.status = status;
    }
}
