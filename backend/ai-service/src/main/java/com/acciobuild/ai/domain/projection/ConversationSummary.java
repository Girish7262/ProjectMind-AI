package com.acciobuild.ai.domain.projection;

import com.acciobuild.ai.enums.ConversationStatus;
import com.acciobuild.ai.enums.ProviderType;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Spring Data Projection for high-performance retrieval of conversation summaries.
 */
public interface ConversationSummary {
    UUID getId();
    UUID getOrganizationId();
    UUID getProjectId();
    String getTitle();
    ConversationStatus getStatus();
    ProviderType getModelProvider();
    String getModelName();
    LocalDateTime getCreatedAt();
}
