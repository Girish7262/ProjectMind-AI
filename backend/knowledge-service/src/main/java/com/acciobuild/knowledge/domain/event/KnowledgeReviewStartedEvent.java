package com.acciobuild.knowledge.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when a document is submitted for review.
 */
@Getter
public class KnowledgeReviewStartedEvent extends KnowledgeDomainEvent {

    private final UUID documentId;

    /**
     * Constructs the event.
     */
    public KnowledgeReviewStartedEvent(UUID organizationId, UUID documentId, String correlationId) {
        super("KNOWLEDGE_REVIEW_STARTED", organizationId, correlationId);
        this.documentId = documentId;
    }
}
