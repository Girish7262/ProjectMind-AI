package com.acciobuild.knowledge.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when a new document version revision is committed.
 */
@Getter
public class KnowledgeVersionCreatedEvent extends KnowledgeDomainEvent {

    private final UUID documentId;
    private final int versionNumber;

    /**
     * Constructs the event.
     */
    public KnowledgeVersionCreatedEvent(UUID organizationId, UUID documentId, int number, String correlationId) {
        super("KNOWLEDGE_VERSION_CREATED", organizationId, correlationId);
        this.documentId = documentId;
        this.versionNumber = number;
    }
}
