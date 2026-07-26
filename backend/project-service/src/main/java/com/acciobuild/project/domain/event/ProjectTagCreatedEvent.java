package com.acciobuild.project.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when a custom project tag classification is defined.
 */
@Getter
public class ProjectTagCreatedEvent extends ProjectDomainEvent {

    private final UUID projectId;
    private final String tagName;

    /**
     * Constructs the event.
     */
    public ProjectTagCreatedEvent(UUID organizationId, UUID projectId, String tag, String correlationId) {
        super("PROJECT_TAG_CREATED", organizationId, correlationId);
        this.projectId = projectId;
        this.tagName = tag;
    }
}
