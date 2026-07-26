package com.acciobuild.knowledge.dto.projection;

import java.util.UUID;

/**
 * Lightweight Spring Data JPA projection for Knowledge Collection records.
 */
public interface KnowledgeCollectionSummary {
    UUID getId();
    String getName();
    String getVisibility();
}
