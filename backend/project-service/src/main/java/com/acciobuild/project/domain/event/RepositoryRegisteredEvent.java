package com.acciobuild.project.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when a new Git repository is registered.
 */
@Getter
public class RepositoryRegisteredEvent extends ProjectDomainEvent {

    private final UUID projectId;
    private final UUID repositoryId;
    private final String url;

    /**
     * Constructs the event.
     */
    public RepositoryRegisteredEvent(UUID organizationId, UUID projectId, UUID repositoryId, String url, String correlationId) {
        super("REPOSITORY_REGISTERED", organizationId, correlationId);
        this.projectId = projectId;
        this.repositoryId = repositoryId;
        this.url = url;
    }
}
