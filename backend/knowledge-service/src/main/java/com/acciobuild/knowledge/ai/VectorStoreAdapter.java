package com.acciobuild.knowledge.ai;

import java.util.List;
import java.util.UUID;

/**
 * Interface mapping to vector database integrations (e.g. pgvector, OpenSearch).
 */
public interface VectorStoreAdapter {

    /**
     * Persist calculated embedding vectors corresponding to document chunks.
     */
    void saveEmbeddings(UUID documentId, List<UUID> chunkIds, List<List<Double>> vectors);

    /**
     * Execute a k-nearest neighbor query against vectors inside the tenant partition boundary.
     */
    List<UUID> similaritySearch(UUID organizationId, List<Double> queryVector, int topK);
}
