package com.acciobuild.ai.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when a message is added.
 */
@Getter
public class MessageAddedEvent extends AiDomainEvent {
    private final UUID conversationId;
    private final UUID messageId;
    private final String role;

    public MessageAddedEvent(UUID organizationId, UUID conversationId, UUID messageId, String role, String correlationId) {
        super("MESSAGE_ADDED", organizationId, correlationId);
        this.conversationId = conversationId;
        this.messageId = messageId;
        this.role = role;
    }
}
