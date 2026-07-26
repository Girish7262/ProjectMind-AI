package com.acciobuild.ai.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when a prompt template is updated.
 */
@Getter
public class PromptUpdatedEvent extends AiDomainEvent {
    private final UUID promptId;
    private final String name;

    public PromptUpdatedEvent(UUID organizationId, UUID promptId, String name, String correlationId) {
        super("PROMPT_UPDATED", organizationId, correlationId);
        this.promptId = promptId;
        this.name = name;
    }
}
