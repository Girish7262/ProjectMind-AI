package com.acciobuild.ai.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when tool execution is requested.
 */
@Getter
public class ToolExecutionRequestedEvent extends AiDomainEvent {
    private final UUID messageId;
    private final String toolName;

    public ToolExecutionRequestedEvent(UUID organizationId, UUID messageId, String toolName, String correlationId) {
        super("TOOL_EXECUTION_REQUESTED", organizationId, correlationId);
        this.messageId = messageId;
        this.toolName = toolName;
    }
}
