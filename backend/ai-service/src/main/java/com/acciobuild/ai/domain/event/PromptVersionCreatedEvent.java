package com.acciobuild.ai.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when a new version of a prompt template is created.
 */
@Getter
public class PromptVersionCreatedEvent extends AiDomainEvent {
    private final UUID promptId;
    private final UUID versionId;
    private final Integer versionNumber;

    public PromptVersionCreatedEvent(UUID organizationId, UUID promptId, UUID versionId, Integer versionNumber, String correlationId) {
        super("PROMPT_VERSION_CREATED", organizationId, correlationId);
        this.promptId = promptId;
        this.versionId = versionId;
        this.versionNumber = versionNumber;
    }
}
