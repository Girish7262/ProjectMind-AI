package com.acciobuild.knowledge.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when an embedding job is successfully queued.
 */
@Getter
public class EmbeddingQueuedEvent extends KnowledgeDomainEvent {

    private final UUID documentId;
    private final UUID jobId;
    private final int priority;

    @com.fasterxml.jackson.annotation.JsonCreator
    public EmbeddingQueuedEvent(
            @com.fasterxml.jackson.annotation.JsonProperty("organizationId") UUID organizationId,
            @com.fasterxml.jackson.annotation.JsonProperty("documentId") UUID documentId,
            @com.fasterxml.jackson.annotation.JsonProperty("jobId") UUID jobId,
            @com.fasterxml.jackson.annotation.JsonProperty("priority") int priority,
            @com.fasterxml.jackson.annotation.JsonProperty("correlationId") String correlationId) {
        super("EMBEDDING_QUEUED", organizationId, correlationId);
        this.documentId = documentId;
        this.jobId = jobId;
        this.priority = priority;
    }
}
