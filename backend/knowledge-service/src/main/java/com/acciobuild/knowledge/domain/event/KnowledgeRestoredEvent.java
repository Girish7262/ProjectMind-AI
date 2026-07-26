package com.acciobuild.knowledge.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when an archived or soft-deleted document is restored.
 */
@Getter
public class KnowledgeRestoredEvent extends KnowledgeDomainEvent {

    private final UUID documentId;

    /**
     * Constructs the event.
     */
    public KnowledgeRestoredEvent(UUID organizationId, UUID documentId, String correlationId) {
        super("KNOWLEDGE_RESTORED", organizationId, correlationId);
        this.documentId = documentId;
    }
}
