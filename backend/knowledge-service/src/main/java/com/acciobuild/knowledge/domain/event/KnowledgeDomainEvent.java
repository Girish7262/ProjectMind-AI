package com.acciobuild.knowledge.domain.event;

import lombok.Getter;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base abstract class representing domain events published by Knowledge Service.
 */
@Getter
public abstract class KnowledgeDomainEvent {

    private final String eventType;
    private final UUID organizationId;
    private final LocalDateTime timestamp;
    private final String correlationId;

    /**
     * Constructs the base event.
     */
    protected KnowledgeDomainEvent(String eventType, UUID organizationId, String correlationId) {
        this.eventType = eventType;
        this.organizationId = organizationId;
        this.timestamp = LocalDateTime.now();
        this.correlationId = correlationId;
    }
}
