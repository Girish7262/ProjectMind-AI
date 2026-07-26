package com.acciobuild.project.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when project threshold settings configs change.
 */
@Getter
public class ProjectConfigurationChangedEvent extends ProjectDomainEvent {

    private final UUID projectId;

    /**
     * Constructs the event.
     */
    public ProjectConfigurationChangedEvent(UUID organizationId, UUID projectId, String correlationId) {
        super("PROJECT_CONFIGURATION_CHANGED", organizationId, correlationId);
        this.projectId = projectId;
    }
}
