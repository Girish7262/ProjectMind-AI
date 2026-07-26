package com.acciobuild.knowledge.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when a Knowledge Document is created.
 */
@Getter
public class KnowledgeDocumentCreatedEvent extends KnowledgeDomainEvent {

    private final UUID documentId;
    private final String slug;

    /**
     * Constructs the event.
     */
    public KnowledgeDocumentCreatedEvent(UUID organizationId, UUID documentId, String slug, String correlationId) {
        super("KNOWLEDGE_DOCUMENT_CREATED", organizationId, correlationId);
        this.documentId = documentId;
        this.slug = slug;
    }
}
