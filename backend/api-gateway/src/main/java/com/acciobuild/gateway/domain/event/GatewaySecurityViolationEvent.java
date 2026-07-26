package com.acciobuild.gateway.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Event published when unauthorized path access or bad tokens are detected.
 */
@Getter
public class GatewaySecurityViolationEvent extends GatewayDomainEvent {
    private final String path;
    private final String details;
    private final String clientIp;

    public GatewaySecurityViolationEvent(UUID organizationId, String path, String details, String clientIp, String correlationId) {
        super("SECURITY_VIOLATION", organizationId, correlationId);
        this.path = path;
        this.details = details;
        this.clientIp = clientIp;
    }
}
