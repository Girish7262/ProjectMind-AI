package com.acciobuild.organization.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when organization settings metadata is updated.
 */
@Getter
public class OrganizationSettingsUpdatedEvent extends OrganizationDomainEvent {

    /**
     * Constructs the event.
     */
    public OrganizationSettingsUpdatedEvent(UUID tenantId, String correlationId) {
        super("ORGANIZATION_SETTINGS_UPDATED", tenantId, correlationId);
    }
}
