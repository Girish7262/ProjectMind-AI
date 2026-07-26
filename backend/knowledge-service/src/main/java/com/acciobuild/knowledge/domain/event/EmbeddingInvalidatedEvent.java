package com.acciobuild.knowledge.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when existing embeddings for a document are invalidated due to content modifications.
 */
@Getter
public class EmbeddingInvalidatedEvent extends KnowledgeDomainEvent {

    private final UUID documentId;
    private final String reason;

    @com.fasterxml.jackson.annotation.JsonCreator
    public EmbeddingInvalidatedEvent(
            @com.fasterxml.jackson.annotation.JsonProperty("organizationId") UUID organizationId,
            @com.fasterxml.jackson.annotation.JsonProperty("documentId") UUID documentId,
            @com.fasterxml.jackson.annotation.JsonProperty("reason") String reason,
            @com.fasterxml.jackson.annotation.JsonProperty("correlationId") String correlationId) {
        super("EMBEDDING_INVALIDATED", organizationId, correlationId);
        this.documentId = documentId;
        this.reason = reason;
    }
}
