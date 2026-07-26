package com.acciobuild.ai.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when sliding window message histories change.
 */
@Getter
public class ConversationWindowUpdatedEvent extends AiDomainEvent {
    private final UUID conversationId;
    private final int messageCount;

    public ConversationWindowUpdatedEvent(UUID organizationId, UUID conversationId, int messageCount, String correlationId) {
        super("CONVERSATION_WINDOW_UPDATED", organizationId, correlationId);
        this.conversationId = conversationId;
        this.messageCount = messageCount;
    }
}
