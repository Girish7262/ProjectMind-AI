package com.acciobuild.knowledge.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when a document is transitioned to ARCHIVED.
 */
@Getter
public class KnowledgeArchivedEvent extends KnowledgeDomainEvent {

    private final UUID documentId;

    /**
     * Constructs the event.
     */
    public KnowledgeArchivedEvent(UUID organizationId, UUID documentId, String correlationId) {
        super("KNOWLEDGE_ARCHIVED", organizationId, correlationId);
        this.documentId = documentId;
    }
}
