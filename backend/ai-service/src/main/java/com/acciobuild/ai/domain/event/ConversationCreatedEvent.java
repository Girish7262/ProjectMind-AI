package com.acciobuild.ai.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when an AI conversation is created.
 */
@Getter
public class ConversationCreatedEvent extends AiDomainEvent {
    private final UUID conversationId;
    private final UUID projectId;

    public ConversationCreatedEvent(UUID organizationId, UUID conversationId, UUID projectId, String correlationId) {
        super("CONVERSATION_CREATED", organizationId, correlationId);
        this.conversationId = conversationId;
        this.projectId = projectId;
    }
}
