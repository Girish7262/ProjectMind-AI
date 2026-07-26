package com.acciobuild.project.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when collaborator membership is revoked.
 */
@Getter
public class ProjectMemberRemovedEvent extends ProjectDomainEvent {

    private final UUID projectId;
    private final UUID userId;

    /**
     * Constructs the event.
     */
    public ProjectMemberRemovedEvent(UUID organizationId, UUID projectId, UUID userId, String correlationId) {
        super("PROJECT_MEMBER_REMOVED", organizationId, correlationId);
        this.projectId = projectId;
        this.userId = userId;
    }
}
