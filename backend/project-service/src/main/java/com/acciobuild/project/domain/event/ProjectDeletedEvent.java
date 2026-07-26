package com.acciobuild.project.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when a project is soft deleted.
 */
@Getter
public class ProjectDeletedEvent extends ProjectDomainEvent {

    private final UUID projectId;

    /**
     * Constructs the event.
     */
    public ProjectDeletedEvent(UUID organizationId, UUID projectId, String correlationId) {
        super("PROJECT_DELETED", organizationId, correlationId);
        this.projectId = projectId;
    }
}
