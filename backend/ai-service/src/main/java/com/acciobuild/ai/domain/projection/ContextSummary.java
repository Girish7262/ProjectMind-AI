package com.acciobuild.ai.domain.projection;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Spring Data Projection mapping query contexts.
 */
public interface ContextSummary {
    UUID getId();
    UUID getOrganizationId();
    UUID getConversationId();
    String getQueryText();
    LocalDateTime getCreatedAt();
}
