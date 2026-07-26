package com.acciobuild.organization.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when a feature flag is disabled.
 */
@Getter
public class FeatureDisabledEvent extends OrganizationDomainEvent {

    private final String feature;

    /**
     * Constructs the event.
     */
    public FeatureDisabledEvent(UUID tenantId, String feature, String correlationId) {
        super("FEATURE_DISABLED", tenantId, correlationId);
        this.feature = feature;
    }
}
