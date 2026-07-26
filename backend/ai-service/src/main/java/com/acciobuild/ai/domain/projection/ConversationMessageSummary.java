package com.acciobuild.ai.domain.projection;

import com.acciobuild.ai.enums.MessageRole;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Spring Data Projection mapping message content and token summaries.
 */
public interface ConversationMessageSummary {
    UUID getId();
    UUID getOrganizationId();
    MessageRole getRole();
    String getContent();
    Integer getTotalTokens();
    LocalDateTime getCreatedAt();
}
