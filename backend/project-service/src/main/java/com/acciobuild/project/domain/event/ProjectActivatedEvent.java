package com.acciobuild.project.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when project state transitions to ACTIVE.
 */
@Getter
public class ProjectActivatedEvent extends ProjectDomainEvent {

    private final UUID projectId;

    /**
     * Constructs the event.
     */
    public ProjectActivatedEvent(UUID organizationId, UUID projectId, String correlationId) {
        super("PROJECT_ACTIVATED", organizationId, correlationId);
        this.projectId = projectId;
    }
}
