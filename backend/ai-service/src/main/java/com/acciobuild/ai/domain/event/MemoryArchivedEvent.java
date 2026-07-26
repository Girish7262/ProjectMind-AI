package com.acciobuild.ai.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when a conversation memory is archived.
 */
@Getter
public class MemoryArchivedEvent extends AiDomainEvent {
    private final UUID memoryId;
    private final UUID conversationId;
    private final String scope;

    public MemoryArchivedEvent(UUID organizationId, UUID memoryId, UUID conversationId, String scope, String correlationId) {
        super("MEMORY_ARCHIVED", organizationId, correlationId);
        this.memoryId = memoryId;
        this.conversationId = conversationId;
        this.scope = scope;
    }
}
