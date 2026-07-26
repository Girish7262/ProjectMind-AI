package com.acciobuild.knowledge.dto;

import com.acciobuild.knowledge.enums.EmbeddingModel;
import com.acciobuild.knowledge.enums.EmbeddingProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * Data Transfer Object representing a request package routed to the embedding service.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmbeddingRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID jobId;
    private UUID documentId;
    private UUID organizationId;
    private EmbeddingProvider provider;
    private EmbeddingModel model;
    private List<ChunkPayload> chunks;

    /**
     * Chunk payload data segment structure.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChunkPayload implements Serializable {
        private static final long serialVersionUID = 1L;
        private UUID chunkId;
        private int chunkIndex;
        private String content;
    }
}
