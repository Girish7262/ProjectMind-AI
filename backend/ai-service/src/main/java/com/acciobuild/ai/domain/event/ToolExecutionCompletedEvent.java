package com.acciobuild.ai.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when tool execution completes.
 */
@Getter
public class ToolExecutionCompletedEvent extends AiDomainEvent {
    private final UUID conversationId;
    private final String toolName;
    private final boolean successful;
    private final long durationMs;

    public ToolExecutionCompletedEvent(UUID organizationId, UUID conversationId, String toolName, boolean successful, long durationMs, String correlationId) {
        super("TOOL_EXECUTION_COMPLETED", organizationId, correlationId);
        this.conversationId = conversationId;
        this.toolName = toolName;
        this.successful = successful;
        this.durationMs = durationMs;
    }
}
