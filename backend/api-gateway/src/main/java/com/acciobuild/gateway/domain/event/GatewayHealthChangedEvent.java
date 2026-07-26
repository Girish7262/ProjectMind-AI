package com.acciobuild.gateway.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Event published when service health checks report status changes.
 */
@Getter
public class GatewayHealthChangedEvent extends GatewayDomainEvent {
    private final String serviceName;
    private final String oldStatus;
    private final String newStatus;

    public GatewayHealthChangedEvent(UUID organizationId, String serviceName, String oldStatus, String newStatus, String correlationId) {
        super("HEALTH_CHANGED", organizationId, correlationId);
        this.serviceName = serviceName;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }
}
