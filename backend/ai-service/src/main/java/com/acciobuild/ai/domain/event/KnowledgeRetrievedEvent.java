package com.acciobuild.ai.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when knowledge search chunks are retrieved and ranked.
 */
@Getter
public class KnowledgeRetrievedEvent extends AiDomainEvent {
    private final UUID conversationId;
    private final int resultsCount;

    public KnowledgeRetrievedEvent(UUID organizationId, UUID conversationId, int resultsCount, String correlationId) {
        super("KNOWLEDGE_RETRIEVED", organizationId, correlationId);
        this.conversationId = conversationId;
        this.resultsCount = resultsCount;
    }
}
