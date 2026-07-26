package com.acciobuild.ai.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when RAG pipeline orchestration fails with an exception.
 */
@Getter
public class ExecutionFailedEvent extends AiDomainEvent {
    private final UUID conversationId;
    private final String stageName;
    private final String errorMessage;

    public ExecutionFailedEvent(UUID organizationId, UUID conversationId, String stageName, String errorMessage, String correlationId) {
        super("EXECUTION_FAILED", organizationId, correlationId);
        this.conversationId = conversationId;
        this.stageName = stageName;
        this.errorMessage = errorMessage;
    }
}
