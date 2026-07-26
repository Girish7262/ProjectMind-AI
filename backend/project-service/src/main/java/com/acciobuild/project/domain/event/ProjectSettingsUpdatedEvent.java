package com.acciobuild.project.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when project settings parameters are updated.
 */
@Getter
public class ProjectSettingsUpdatedEvent extends ProjectDomainEvent {

    private final UUID projectId;

    /**
     * Constructs the event.
     */
    public ProjectSettingsUpdatedEvent(UUID organizationId, UUID projectId, String correlationId) {
        super("PROJECT_SETTINGS_UPDATED", organizationId, correlationId);
        this.projectId = projectId;
    }
}
