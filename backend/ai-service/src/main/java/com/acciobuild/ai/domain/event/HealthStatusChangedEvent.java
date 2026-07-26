package com.acciobuild.ai.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when Actuator custom health state flips.
 */
@Getter
public class HealthStatusChangedEvent extends AiDomainEvent {
    private final String serviceName;
    private final String oldStatus;
    private final String newStatus;

    public HealthStatusChangedEvent(UUID organizationId, String serviceName, String oldStatus, String newStatus, String correlationId) {
        super("HEALTH_STATUS_CHANGED", organizationId, correlationId);
        this.serviceName = serviceName;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }
}
