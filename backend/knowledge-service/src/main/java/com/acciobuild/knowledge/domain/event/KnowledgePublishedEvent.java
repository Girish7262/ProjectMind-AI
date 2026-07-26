package com.acciobuild.knowledge.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when a document is successfully transitions to PUBLISHED.
 */
@Getter
public class KnowledgePublishedEvent extends KnowledgeDomainEvent {

    private final UUID documentId;

    /**
     * Constructs the event.
     */
    @com.fasterxml.jackson.annotation.JsonCreator
    public KnowledgePublishedEvent(
            @com.fasterxml.jackson.annotation.JsonProperty("organizationId") UUID organizationId,
            @com.fasterxml.jackson.annotation.JsonProperty("documentId") UUID documentId,
            @com.fasterxml.jackson.annotation.JsonProperty("correlationId") String correlationId) {
        super("KNOWLEDGE_PUBLISHED", organizationId, correlationId);
        this.documentId = documentId;
    }
}
