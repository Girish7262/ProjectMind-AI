package com.acciobuild.ai.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when RAG pipeline orchestration starts.
 */
@Getter
public class ExecutionStartedEvent extends AiDomainEvent {
    private final UUID conversationId;
    private final String queryText;

    public ExecutionStartedEvent(UUID organizationId, UUID conversationId, String queryText, String correlationId) {
        super("EXECUTION_STARTED", organizationId, correlationId);
        this.conversationId = conversationId;
        this.queryText = queryText;
    }
}
