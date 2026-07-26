package com.acciobuild.ai.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when two memory keys are merged.
 */
@Getter
public class MemoryMergedEvent extends AiDomainEvent {
    private final UUID conversationId;
    private final String scope;
    private final String sourceKey;
    private final String targetKey;

    public MemoryMergedEvent(UUID organizationId, UUID conversationId, String scope, String sourceKey, String targetKey, String correlationId) {
        super("MEMORY_MERGED", organizationId, correlationId);
        this.conversationId = conversationId;
        this.scope = scope;
        this.sourceKey = sourceKey;
        this.targetKey = targetKey;
    }
}
