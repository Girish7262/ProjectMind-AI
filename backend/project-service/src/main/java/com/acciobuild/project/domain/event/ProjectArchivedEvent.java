package com.acciobuild.project.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when a project is archived (read-only).
 */
@Getter
public class ProjectArchivedEvent extends ProjectDomainEvent {

    private final UUID projectId;

    /**
     * Constructs the event.
     */
    public ProjectArchivedEvent(UUID organizationId, UUID projectId, String correlationId) {
        super("PROJECT_ARCHIVED", organizationId, correlationId);
        this.projectId = projectId;
    }
}
