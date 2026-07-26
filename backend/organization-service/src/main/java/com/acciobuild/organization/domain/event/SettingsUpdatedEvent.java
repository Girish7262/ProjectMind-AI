package com.acciobuild.organization.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when organization settings boundaries are updated.
 */
@Getter
public class SettingsUpdatedEvent extends OrganizationDomainEvent {

    private final boolean aiEnabled;
    private final boolean knowledgeSharingEnabled;
    private final String defaultVisibility;

    /**
     * Constructs the event.
     */
    public SettingsUpdatedEvent(UUID tenantId, boolean ai, boolean ks, String vis, String correlationId) {
        super("SETTINGS_UPDATED", tenantId, correlationId);
        this.aiEnabled = ai;
        this.knowledgeSharingEnabled = ks;
        this.defaultVisibility = vis;
    }
}
