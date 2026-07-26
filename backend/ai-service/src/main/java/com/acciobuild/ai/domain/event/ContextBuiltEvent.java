package com.acciobuild.ai.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when RAG context is built.
 */
@Getter
public class ContextBuiltEvent extends AiDomainEvent {
    private final UUID contextId;
    private final UUID conversationId;

    public ContextBuiltEvent(UUID organizationId, UUID contextId, UUID conversationId, String correlationId) {
        super("CONTEXT_BUILT", organizationId, correlationId);
        this.contextId = contextId;
        this.conversationId = conversationId;
    }
}
