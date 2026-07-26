package com.acciobuild.gateway.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Event published when users, tenants, or IPs trigger rate limiters.
 */
@Getter
public class GatewayRateLimitExceededEvent extends GatewayDomainEvent {
    private final String limitKey;
    private final String clientIp;

    public GatewayRateLimitExceededEvent(UUID organizationId, String limitKey, String clientIp, String correlationId) {
        super("RATE_LIMIT_EXCEEDED", organizationId, correlationId);
        this.limitKey = limitKey;
        this.clientIp = clientIp;
    }
}
