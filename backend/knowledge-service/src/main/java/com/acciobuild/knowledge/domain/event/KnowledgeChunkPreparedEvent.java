package com.acciobuild.knowledge.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when document contents are partitioned into text chunks.
 */
@Getter
public class KnowledgeChunkPreparedEvent extends KnowledgeDomainEvent {

    private final UUID documentId;
    private final int chunkCount;

    /**
     * Constructs the event.
     */
    @com.fasterxml.jackson.annotation.JsonCreator
    public KnowledgeChunkPreparedEvent(
            @com.fasterxml.jackson.annotation.JsonProperty("organizationId") UUID organizationId,
            @com.fasterxml.jackson.annotation.JsonProperty("documentId") UUID documentId,
            @com.fasterxml.jackson.annotation.JsonProperty("chunkCount") int chunkCount,
            @com.fasterxml.jackson.annotation.JsonProperty("correlationId") String correlationId) {
        super("KNOWLEDGE_CHUNK_PREPARED", organizationId, correlationId);
        this.documentId = documentId;
        this.chunkCount = chunkCount;
    }
}
