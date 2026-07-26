package com.acciobuild.ai.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when conversation memory entries expire.
 */
@Getter
public class MemoryExpiredEvent extends AiDomainEvent {
    private final UUID conversationId;
    private final String scope;
    private final String memoryKey;

    public MemoryExpiredEvent(UUID organizationId, UUID conversationId, String scope, String memoryKey, String correlationId) {
        super("MEMORY_EXPIRED", organizationId, correlationId);
        this.conversationId = conversationId;
        this.scope = scope;
        this.memoryKey = memoryKey;
    }
}
