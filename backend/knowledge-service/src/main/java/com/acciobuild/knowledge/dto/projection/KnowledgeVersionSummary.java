package com.acciobuild.knowledge.dto.projection;

import java.util.UUID;

/**
 * Lightweight Spring Data JPA projection for Knowledge Version histories.
 */
public interface KnowledgeVersionSummary {
    UUID getId();
    int getVersionNumber();
    String getContentHash();
    String getChangeSummary();
}
