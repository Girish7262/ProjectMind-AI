package com.acciobuild.ai.domain.projection;

import com.acciobuild.ai.enums.MemoryScope;
import java.util.UUID;

/**
 * Spring Data Projection mapping memory variables.
 */
public interface MemorySummary {
    UUID getId();
    UUID getOrganizationId();
    UUID getConversationId();
    MemoryScope getMemoryScope();
    String getMemoryKey();
    String getMemoryValue();
}
