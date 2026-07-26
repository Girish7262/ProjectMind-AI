package com.acciobuild.project.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when project collaborator is suspended (BLOCKED).
 */
@Getter
public class ProjectMemberDeactivatedEvent extends ProjectDomainEvent {

    private final UUID projectId;
    private final UUID userId;

    /**
     * Constructs the event.
     */
    public ProjectMemberDeactivatedEvent(UUID organizationId, UUID projectId, UUID userId, String correlationId) {
        super("PROJECT_MEMBER_DEACTIVATED", organizationId, correlationId);
        this.projectId = projectId;
        this.userId = userId;
    }
}
