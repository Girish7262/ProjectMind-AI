package com.acciobuild.ai.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when a conversation is soft deleted or permanently removed.
 */
@Getter
public class ConversationDeletedEvent extends AiDomainEvent {
    private final UUID conversationId;

    public ConversationDeletedEvent(UUID organizationId, UUID conversationId, String correlationId) {
        super("CONVERSATION_DELETED", organizationId, correlationId);
        this.conversationId = conversationId;
    }
}
