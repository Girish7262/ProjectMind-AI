package com.acciobuild.gateway.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Event published when a gateway request exceeds execution limits.
 */
@Getter
public class GatewaySlowRequestEvent extends GatewayDomainEvent {
    private final String path;
    private final String method;
    private final long latencyMs;

    public GatewaySlowRequestEvent(UUID organizationId, String path, String method, long latencyMs, String correlationId) {
        super("SLOW_REQUEST", organizationId, correlationId);
        this.path = path;
        this.method = method;
        this.latencyMs = latencyMs;
    }
}
