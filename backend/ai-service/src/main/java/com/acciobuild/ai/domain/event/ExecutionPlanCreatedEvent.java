package com.acciobuild.ai.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when an immutable execution plan is finalized.
 */
@Getter
public class ExecutionPlanCreatedEvent extends AiDomainEvent {
    private final UUID conversationId;
    private final UUID planId;
    private final String selectedProvider;
    private final String selectedModel;

    public ExecutionPlanCreatedEvent(UUID organizationId, UUID conversationId, UUID planId, String selectedProvider, String selectedModel, String correlationId) {
        super("EXECUTION_PLAN_CREATED", organizationId, correlationId);
        this.conversationId = conversationId;
        this.planId = planId;
        this.selectedProvider = selectedProvider;
        this.selectedModel = selectedModel;
    }
}
