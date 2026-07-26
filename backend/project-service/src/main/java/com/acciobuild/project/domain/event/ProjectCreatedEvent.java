package com.acciobuild.project.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when a new project is created.
 */
@Getter
public class ProjectCreatedEvent extends ProjectDomainEvent {

    private final UUID projectId;
    private final String projectCode;

    /**
     * Constructs the event.
     */
    public ProjectCreatedEvent(UUID organizationId, UUID projectId, String code, String correlationId) {
        super("PROJECT_CREATED", organizationId, correlationId);
        this.projectId = projectId;
        this.projectCode = code;
    }
}
