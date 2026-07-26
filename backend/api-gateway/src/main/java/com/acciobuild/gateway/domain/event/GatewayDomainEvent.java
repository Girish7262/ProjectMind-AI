package com.acciobuild.gateway.domain.event;

import lombok.Getter;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base abstract class representing domain events published by the API Gateway.
 */
@Getter
public abstract class GatewayDomainEvent {
    private final String eventType;
    private final UUID organizationId;
    private final LocalDateTime timestamp;
    private final String correlationId;

    protected GatewayDomainEvent(String eventType, UUID organizationId, String correlationId) {
        this.eventType = eventType;
        this.organizationId = organizationId;
        this.timestamp = LocalDateTime.now();
        this.correlationId = correlationId;
    }
}
