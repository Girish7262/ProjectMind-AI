package com.acciobuild.project.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when a Git repository registration is deleted.
 */
@Getter
public class RepositoryDeletedEvent extends ProjectDomainEvent {

    private final UUID projectId;

    /**
     * Constructs the event.
     */
    public RepositoryDeletedEvent(UUID organizationId, UUID projectId, String correlationId) {
        super("REPOSITORY_DELETED", organizationId, correlationId);
        this.projectId = projectId;
    }
}
