package com.acciobuild.ai.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when the RAG prompt context builder pipeline compiles successfully.
 */
@Getter
public class ConversationContextBuiltEvent extends AiDomainEvent {
    private final UUID conversationId;
    private final int contextChunkCount;

    public ConversationContextBuiltEvent(UUID organizationId, UUID conversationId, int contextChunkCount, String correlationId) {
        super("CONVERSATION_CONTEXT_BUILT", organizationId, correlationId);
        this.conversationId = conversationId;
        this.contextChunkCount = contextChunkCount;
    }
}
