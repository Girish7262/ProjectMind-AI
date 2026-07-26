package com.acciobuild.ai.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when an orchestration stage finishes execution.
 */
@Getter
public class ExecutionStageCompletedEvent extends AiDomainEvent {
    private final UUID conversationId;
    private final String stageName;
    private final long durationMs;

    public ExecutionStageCompletedEvent(UUID organizationId, UUID conversationId, String stageName, long durationMs, String correlationId) {
        super("EXECUTION_STAGE_COMPLETED", organizationId, correlationId);
        this.conversationId = conversationId;
        this.stageName = stageName;
        this.durationMs = durationMs;
    }
}
