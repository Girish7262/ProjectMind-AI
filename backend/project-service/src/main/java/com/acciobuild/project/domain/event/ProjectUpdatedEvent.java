package com.acciobuild.project.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when project profile metadata is updated.
 */
@Getter
public class ProjectUpdatedEvent extends ProjectDomainEvent {

    private final UUID projectId;

    /**
     * Constructs the event.
     */
    public ProjectUpdatedEvent(UUID organizationId, UUID projectId, String correlationId) {
        super("PROJECT_UPDATED", organizationId, correlationId);
        this.projectId = projectId;
    }
}
