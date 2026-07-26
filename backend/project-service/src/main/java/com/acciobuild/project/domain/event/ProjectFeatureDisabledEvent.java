package com.acciobuild.project.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when a project feature flag is disabled.
 */
@Getter
public class ProjectFeatureDisabledEvent extends ProjectDomainEvent {

    private final UUID projectId;
    private final String feature;

    /**
     * Constructs the event.
     */
    public ProjectFeatureDisabledEvent(UUID organizationId, UUID projectId, String feature, String correlationId) {
        super("PROJECT_FEATURE_DISABLED", organizationId, correlationId);
        this.projectId = projectId;
        this.feature = feature;
    }
}
