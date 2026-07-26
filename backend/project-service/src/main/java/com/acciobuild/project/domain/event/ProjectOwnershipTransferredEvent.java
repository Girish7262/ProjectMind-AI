package com.acciobuild.project.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when project maintainer/owner role is transferred.
 */
@Getter
public class ProjectOwnershipTransferredEvent extends ProjectDomainEvent {

    private final UUID projectId;
    private final UUID fromUserId;
    private final UUID toUserId;

    /**
     * Constructs the event.
     */
    public ProjectOwnershipTransferredEvent(UUID organizationId, UUID projectId, UUID fromUserId, UUID toUserId, String correlationId) {
        super("PROJECT_OWNERSHIP_TRANSFERRED", organizationId, correlationId);
        this.projectId = projectId;
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
    }
}
