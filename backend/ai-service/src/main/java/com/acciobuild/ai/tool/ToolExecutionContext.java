package com.acciobuild.ai.tool;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

/**
 * Context container enclosing tracing, session state, and security boundaries for tool executions.
 */
@Value
@Builder
public class ToolExecutionContext {
    UUID conversationId;
    UUID organizationId;
    String traceId;
}
