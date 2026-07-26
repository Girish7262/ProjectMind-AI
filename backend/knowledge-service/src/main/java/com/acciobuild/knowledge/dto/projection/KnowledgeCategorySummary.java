package com.acciobuild.knowledge.dto.projection;

import java.util.UUID;

/**
 * Lightweight Spring Data JPA projection for Knowledge Category records.
 */
public interface KnowledgeCategorySummary {
    UUID getId();
    String getName();
    String getColor();
    String getIcon();
}
