package com.acciobuild.ai.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when tool execution begins.
 */
@Getter
public class ToolExecutionStartedEvent extends AiDomainEvent {
    private final UUID conversationId;
    private final String toolName;

    public ToolExecutionStartedEvent(UUID organizationId, UUID conversationId, String toolName, String correlationId) {
        super("TOOL_EXECUTION_STARTED", organizationId, correlationId);
        this.conversationId = conversationId;
        this.toolName = toolName;
    }
}
