package com.acciobuild.gateway.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Event published when a gateway request completes.
 */
@Getter
public class GatewayRequestCompletedEvent extends GatewayDomainEvent {
    private final String path;
    private final String method;
    private final int status;
    private final long latencyMs;

    public GatewayRequestCompletedEvent(UUID organizationId, String path, String method, int status, long latencyMs, String correlationId) {
        super("REQUEST_COMPLETED", organizationId, correlationId);
        this.path = path;
        this.method = method;
        this.status = status;
        this.latencyMs = latencyMs;
    }
}
