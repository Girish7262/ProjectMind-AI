package com.acciobuild.ai.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when a prompt template is registered.
 */
@Getter
public class PromptTemplateCreatedEvent extends AiDomainEvent {
    private final UUID templateId;
    private final String name;

    public PromptTemplateCreatedEvent(UUID organizationId, UUID templateId, String name, String correlationId) {
        super("PROMPT_TEMPLATE_CREATED", organizationId, correlationId);
        this.templateId = templateId;
        this.name = name;
    }
}
