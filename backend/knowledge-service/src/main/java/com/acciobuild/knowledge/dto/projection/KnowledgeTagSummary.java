package com.acciobuild.knowledge.dto.projection;

import java.util.UUID;

/**
 * Lightweight Spring Data JPA projection for Knowledge Tag records.
 */
public interface KnowledgeTagSummary {
    UUID getId();
    String getName();
    String getColor();
}
