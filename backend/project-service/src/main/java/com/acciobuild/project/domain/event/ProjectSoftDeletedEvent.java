package com.acciobuild.project.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when project state transitions to DELETED.
 */
@Getter
public class ProjectSoftDeletedEvent extends ProjectDomainEvent {

    private final UUID projectId;

    /**
     * Constructs the event.
     */
    public ProjectSoftDeletedEvent(UUID organizationId, UUID projectId, String correlationId) {
        super("PROJECT_SOFT_DELETED", organizationId, correlationId);
        this.projectId = projectId;
    }
}
