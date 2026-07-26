package com.acciobuild.knowledge.service;

import java.util.UUID;

/**
 * Service contract managing full-text keyword indexing and search preparation.
 */
public interface IndexPreparationService {

    /**
     * Compiles and weights a document's searchable text corpus index.
     */
    void buildIndex(UUID documentId);
}
