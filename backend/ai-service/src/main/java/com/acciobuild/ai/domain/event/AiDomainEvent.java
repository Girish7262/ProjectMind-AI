package com.acciobuild.ai.domain.event;

import lombok.Getter;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base abstract class representing domain events published by the AI Service.
 */
@Getter
public abstract class AiDomainEvent {

    private final String eventType;
    private final UUID organizationId;
    private final LocalDateTime timestamp;
    private final String correlationId;

    protected AiDomainEvent(String eventType, UUID organizationId, String correlationId) {
        this.eventType = eventType;
        this.organizationId = organizationId;
        this.timestamp = LocalDateTime.now();
        this.correlationId = correlationId;
    }
}
