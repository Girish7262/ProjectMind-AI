package com.acciobuild.knowledge.dto.projection;

import java.util.UUID;

/**
 * Lightweight Spring Data JPA projection for Knowledge Document lists.
 */
public interface KnowledgeDocumentSummary {
    UUID getId();
    UUID getProjectId();
    UUID getOrganizationId();
    String getTitle();
    String getSlug();
    String getStatus();
    String getVisibility();
}
