package com.acciobuild.organization.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when a feature flag is enabled.
 */
@Getter
public class FeatureEnabledEvent extends OrganizationDomainEvent {

    private final String feature;

    /**
     * Constructs the event.
     */
    public FeatureEnabledEvent(UUID tenantId, String feature, String correlationId) {
        super("FEATURE_ENABLED", tenantId, correlationId);
        this.feature = feature;
    }
}
