package com.acciobuild.knowledge.service;

import java.util.UUID;

/**
 * Service contract managing partitioning document payloads into searchable text chunks.
 */
public interface ChunkPreparationService {

    /**
     * Splits doc content into smaller partitions with overlaps and records them.
     */
    void prepareChunks(UUID documentId, String content);
}
