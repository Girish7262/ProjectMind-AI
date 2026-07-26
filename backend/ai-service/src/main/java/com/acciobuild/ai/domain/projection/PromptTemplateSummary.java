package com.acciobuild.ai.domain.projection;

import com.acciobuild.ai.enums.PromptStatus;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Spring Data Projection mapping template settings.
 */
public interface PromptTemplateSummary {
    UUID getId();
    UUID getOrganizationId();
    String getName();
    String getDescription();
    PromptStatus getStatus();
    LocalDateTime getCreatedAt();
}
