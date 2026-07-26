package com.acciobuild.knowledge.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when embedding generation for a document is successfully completed.
 */
@Getter
public class EmbeddingCompletedEvent extends KnowledgeDomainEvent {

    private final UUID documentId;
    private final UUID jobId;
    private final int chunkCount;
    private final double cost;

    @com.fasterxml.jackson.annotation.JsonCreator
    public EmbeddingCompletedEvent(
            @com.fasterxml.jackson.annotation.JsonProperty("organizationId") UUID organizationId,
            @com.fasterxml.jackson.annotation.JsonProperty("documentId") UUID documentId,
            @com.fasterxml.jackson.annotation.JsonProperty("jobId") UUID jobId,
            @com.fasterxml.jackson.annotation.JsonProperty("chunkCount") int chunkCount,
            @com.fasterxml.jackson.annotation.JsonProperty("cost") double cost,
            @com.fasterxml.jackson.annotation.JsonProperty("correlationId") String correlationId) {
        super("EMBEDDING_COMPLETED", organizationId, correlationId);
        this.documentId = documentId;
        this.jobId = jobId;
        this.chunkCount = chunkCount;
        this.cost = cost;
    }
}
