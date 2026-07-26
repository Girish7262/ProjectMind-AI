package com.acciobuild.project.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when project state transitions to SUSPENDED.
 */
@Getter
public class ProjectSuspendedEvent extends ProjectDomainEvent {

    private final UUID projectId;

    /**
     * Constructs the event.
     */
    public ProjectSuspendedEvent(UUID organizationId, UUID projectId, String correlationId) {
        super("PROJECT_SUSPENDED", organizationId, correlationId);
        this.projectId = projectId;
    }
}
