package com.acciobuild.ai.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when conversation memory is created.
 */
@Getter
public class MemoryCreatedEvent extends AiDomainEvent {
    private final UUID memoryId;
    private final UUID conversationId;
    private final String scope;
    private final String memoryKey;

    public MemoryCreatedEvent(UUID organizationId, UUID memoryId, UUID conversationId, String scope, String memoryKey, String correlationId) {
        super("MEMORY_CREATED", organizationId, correlationId);
        this.memoryId = memoryId;
        this.conversationId = conversationId;
        this.scope = scope;
        this.memoryKey = memoryKey;
    }
}
