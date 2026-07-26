package com.acciobuild.ai.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when a prompt template is created.
 */
@Getter
public class PromptCreatedEvent extends AiDomainEvent {
    private final UUID promptId;
    private final String name;

    public PromptCreatedEvent(UUID organizationId, UUID promptId, String name, String correlationId) {
        super("PROMPT_CREATED", organizationId, correlationId);
        this.promptId = promptId;
        this.name = name;
    }
}
