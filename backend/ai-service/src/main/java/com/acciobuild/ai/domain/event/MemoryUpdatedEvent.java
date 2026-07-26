package com.acciobuild.ai.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when conversation memory scope is modified.
 */
@Getter
public class MemoryUpdatedEvent extends AiDomainEvent {
    private final UUID conversationId;
    private final String scope;

    public MemoryUpdatedEvent(UUID organizationId, UUID conversationId, String scope, String correlationId) {
        super("MEMORY_UPDATED", organizationId, correlationId);
        this.conversationId = conversationId;
        this.scope = scope;
    }
}
