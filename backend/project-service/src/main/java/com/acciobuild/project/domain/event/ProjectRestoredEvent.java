package com.acciobuild.project.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when an archived or soft-deleted project is restored.
 */
@Getter
public class ProjectRestoredEvent extends ProjectDomainEvent {

    private final UUID projectId;

    /**
     * Constructs the event.
     */
    public ProjectRestoredEvent(UUID organizationId, UUID projectId, String correlationId) {
        super("PROJECT_RESTORED", organizationId, correlationId);
        this.projectId = projectId;
    }
}
