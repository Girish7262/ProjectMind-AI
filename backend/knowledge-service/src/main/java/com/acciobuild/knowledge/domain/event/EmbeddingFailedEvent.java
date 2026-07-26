package com.acciobuild.knowledge.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when embedding generation fails permanently or hits max retries.
 */
@Getter
public class EmbeddingFailedEvent extends KnowledgeDomainEvent {

    private final UUID documentId;
    private final UUID jobId;
    private final String cause;

    @com.fasterxml.jackson.annotation.JsonCreator
    public EmbeddingFailedEvent(
            @com.fasterxml.jackson.annotation.JsonProperty("organizationId") UUID organizationId,
            @com.fasterxml.jackson.annotation.JsonProperty("documentId") UUID documentId,
            @com.fasterxml.jackson.annotation.JsonProperty("jobId") UUID jobId,
            @com.fasterxml.jackson.annotation.JsonProperty("cause") String cause,
            @com.fasterxml.jackson.annotation.JsonProperty("correlationId") String correlationId) {
        super("EMBEDDING_FAILED", organizationId, correlationId);
        this.documentId = documentId;
        this.jobId = jobId;
        this.cause = cause;
    }
}
