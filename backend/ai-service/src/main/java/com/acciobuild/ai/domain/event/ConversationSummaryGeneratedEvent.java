package com.acciobuild.ai.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when conversation summary stubs are generated.
 */
@Getter
public class ConversationSummaryGeneratedEvent extends AiDomainEvent {
    private final UUID conversationId;
    private final String summaryText;

    public ConversationSummaryGeneratedEvent(UUID organizationId, UUID conversationId, String summaryText, String correlationId) {
        super("CONVERSATION_SUMMARY_GENERATED", organizationId, correlationId);
        this.conversationId = conversationId;
        this.summaryText = summaryText;
    }
}
