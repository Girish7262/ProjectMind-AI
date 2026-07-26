package com.acciobuild.project.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when project collaborator is activated (ACTIVE).
 */
@Getter
public class ProjectMemberActivatedEvent extends ProjectDomainEvent {

    private final UUID projectId;
    private final UUID userId;

    /**
     * Constructs the event.
     */
    public ProjectMemberActivatedEvent(UUID organizationId, UUID projectId, UUID userId, String correlationId) {
        super("PROJECT_MEMBER_ACTIVATED", organizationId, correlationId);
        this.projectId = projectId;
        this.userId = userId;
    }
}
