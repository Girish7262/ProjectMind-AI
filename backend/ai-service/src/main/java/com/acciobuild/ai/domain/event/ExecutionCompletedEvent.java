package com.acciobuild.ai.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when RAG pipeline orchestration finishes successfully.
 */
@Getter
public class ExecutionCompletedEvent extends AiDomainEvent {
    private final UUID conversationId;
    private final long totalDurationMs;

    public ExecutionCompletedEvent(UUID organizationId, UUID conversationId, long totalDurationMs, String correlationId) {
        super("EXECUTION_COMPLETED", organizationId, correlationId);
        this.conversationId = conversationId;
        this.totalDurationMs = totalDurationMs;
    }
}
