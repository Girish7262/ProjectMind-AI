package com.acciobuild.project.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when a user is added as collaborator to a project.
 */
@Getter
public class ProjectMemberAddedEvent extends ProjectDomainEvent {

    private final UUID projectId;
    private final UUID userId;
    private final String role;

    /**
     * Constructs the event.
     */
    public ProjectMemberAddedEvent(UUID organizationId, UUID projectId, UUID userId, String role, String correlationId) {
        super("PROJECT_MEMBER_ADDED", organizationId, correlationId);
        this.projectId = projectId;
        this.userId = userId;
        this.role = role;
    }
}
