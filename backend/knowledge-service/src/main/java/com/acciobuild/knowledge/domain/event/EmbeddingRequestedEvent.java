package com.acciobuild.knowledge.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when an embedding generation request is initialized.
 */
@Getter
public class EmbeddingRequestedEvent extends KnowledgeDomainEvent {

    private final UUID documentId;
    private final UUID jobId;

    @com.fasterxml.jackson.annotation.JsonCreator
    public EmbeddingRequestedEvent(
            @com.fasterxml.jackson.annotation.JsonProperty("organizationId") UUID organizationId,
            @com.fasterxml.jackson.annotation.JsonProperty("documentId") UUID documentId,
            @com.fasterxml.jackson.annotation.JsonProperty("jobId") UUID jobId,
            @com.fasterxml.jackson.annotation.JsonProperty("correlationId") String correlationId) {
        super("EMBEDDING_REQUESTED", organizationId, correlationId);
        this.documentId = documentId;
        this.jobId = jobId;
    }
}
