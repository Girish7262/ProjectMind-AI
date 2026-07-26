package com.acciobuild.ai.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when a conversation is archived.
 */
@Getter
public class ConversationArchivedEvent extends AiDomainEvent {
    private final UUID conversationId;

    public ConversationArchivedEvent(UUID organizationId, UUID conversationId, String correlationId) {
        super("CONVERSATION_ARCHIVED", organizationId, correlationId);
        this.conversationId = conversationId;
    }
}
