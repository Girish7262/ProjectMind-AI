package com.acciobuild.organization.domain.event;

import lombok.Getter;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base abstract class representing domain events published by the Organization Service.
 * Formatted to support future serialization and Kafka dispatching.
 */
@Getter
public abstract class OrganizationDomainEvent {

    private final UUID eventId = UUID.randomUUID();
    private final LocalDateTime timestamp = LocalDateTime.now();
    private final String eventType;
    private final UUID tenantId;
    private final String correlationId;

    /**
     * Constructs a domain event.
     */
    protected OrganizationDomainEvent(String eventType, UUID tenantId, String correlationId) {
        this.eventType = eventType;
        this.tenantId = tenantId;
        this.correlationId = correlationId;
    }
}
