package com.acciobuild.organization.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when an organization is suspended.
 */
@Getter
public class OrganizationSuspendedEvent extends OrganizationDomainEvent {

    /**
     * Constructs the event.
     */
    public OrganizationSuspendedEvent(UUID tenantId, String correlationId) {
        super("ORGANIZATION_SUSPENDED", tenantId, correlationId);
    }
}
