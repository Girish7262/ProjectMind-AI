package com.acciobuild.ai.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when a template version becomes active.
 */
@Getter
public class PromptTemplateActivatedEvent extends AiDomainEvent {
    private final UUID templateId;
    private final Integer versionNumber;

    public PromptTemplateActivatedEvent(UUID organizationId, UUID templateId, Integer versionNumber, String correlationId) {
        super("PROMPT_TEMPLATE_ACTIVATED", organizationId, correlationId);
        this.templateId = templateId;
        this.versionNumber = versionNumber;
    }
}
