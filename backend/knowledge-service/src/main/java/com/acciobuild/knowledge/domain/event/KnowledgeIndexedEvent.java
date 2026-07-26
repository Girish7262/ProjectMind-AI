package com.acciobuild.knowledge.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when document contents are parsed and search-indexed.
 */
@Getter
public class KnowledgeIndexedEvent extends KnowledgeDomainEvent {

    private final UUID documentId;

    /**
     * Constructs the event.
     */
    @com.fasterxml.jackson.annotation.JsonCreator
    public KnowledgeIndexedEvent(
            @com.fasterxml.jackson.annotation.JsonProperty("organizationId") UUID organizationId,
            @com.fasterxml.jackson.annotation.JsonProperty("documentId") UUID documentId,
            @com.fasterxml.jackson.annotation.JsonProperty("correlationId") String correlationId) {
        super("KNOWLEDGE_INDEXED", organizationId, correlationId);
        this.documentId = documentId;
    }
}
