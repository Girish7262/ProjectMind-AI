package com.acciobuild.ai.domain.projection;

import com.acciobuild.ai.enums.CitationType;
import java.util.UUID;

/**
 * Spring Data Projection mapping references.
 */
public interface CitationSummary {
    UUID getId();
    UUID getOrganizationId();
    UUID getMessageId();
    CitationType getCitationType();
    UUID getSourceId();
    String getTitle();
    String getUrl();
    String getSnippet();
}
