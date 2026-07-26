package com.acciobuild.ai.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when conversation tokens are calculated.
 */
@Getter
public class ConversationTokenEstimatedEvent extends AiDomainEvent {
    private final UUID conversationId;
    private final int estimatedTokens;

    public ConversationTokenEstimatedEvent(UUID organizationId, UUID conversationId, int estimatedTokens, String correlationId) {
        super("CONVERSATION_TOKEN_ESTIMATED", organizationId, correlationId);
        this.conversationId = conversationId;
        this.estimatedTokens = estimatedTokens;
    }
}
