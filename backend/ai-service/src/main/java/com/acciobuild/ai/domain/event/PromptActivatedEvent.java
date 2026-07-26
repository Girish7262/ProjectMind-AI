package com.acciobuild.ai.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when a prompt template version is activated.
 */
@Getter
public class PromptActivatedEvent extends AiDomainEvent {
    private final UUID promptId;
    private final Integer versionNumber;

    public PromptActivatedEvent(UUID organizationId, UUID promptId, Integer versionNumber, String correlationId) {
        super("PROMPT_ACTIVATED", organizationId, correlationId);
        this.promptId = promptId;
        this.versionNumber = versionNumber;
    }
}
