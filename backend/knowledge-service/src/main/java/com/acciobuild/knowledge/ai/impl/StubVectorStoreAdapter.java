package com.acciobuild.knowledge.ai.impl;

import com.acciobuild.knowledge.ai.VectorStoreAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Stub implementation of VectorStoreAdapter simulating vector storage.
 */
@Component
@Slf4j
public class StubVectorStoreAdapter implements VectorStoreAdapter {

    @Override
    public void saveEmbeddings(UUID documentId, List<UUID> chunkIds, List<List<Double>> vectors) {
        log.info("Stub Vector Store: Saved {} vectors of size {} for document ID: {}", 
                 vectors.size(), vectors.isEmpty() ? 0 : vectors.get(0).size(), documentId);
    }

    @Override
    public List<UUID> similaritySearch(UUID organizationId, List<Double> queryVector, int topK) {
        log.info("Stub Vector Store: similaritySearch for organization: {} (topK={})", organizationId, topK);
        return Collections.emptyList();
    }
}
