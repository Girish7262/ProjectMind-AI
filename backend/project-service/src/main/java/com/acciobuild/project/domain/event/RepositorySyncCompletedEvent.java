package com.acciobuild.project.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when repository sync finishes successfully.
 */
@Getter
public class RepositorySyncCompletedEvent extends ProjectDomainEvent {

    private final UUID repositoryId;
    private final int commitCount;

    /**
     * Constructs the event.
     */
    public RepositorySyncCompletedEvent(UUID organizationId, UUID repositoryId, int commitCount, String correlationId) {
        super("REPOSITORY_SYNC_COMPLETED", organizationId, correlationId);
        this.repositoryId = repositoryId;
        this.commitCount = commitCount;
    }
}
