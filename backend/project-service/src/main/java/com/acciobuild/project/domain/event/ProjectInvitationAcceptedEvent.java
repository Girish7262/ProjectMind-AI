package com.acciobuild.project.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when project collaborator accept invitation.
 */
@Getter
public class ProjectInvitationAcceptedEvent extends ProjectDomainEvent {

    private final UUID projectId;
    private final UUID userId;

    /**
     * Constructs the event.
     */
    public ProjectInvitationAcceptedEvent(UUID organizationId, UUID projectId, UUID userId, String correlationId) {
        super("PROJECT_INVITATION_ACCEPTED", organizationId, correlationId);
        this.projectId = projectId;
        this.userId = userId;
    }
}
