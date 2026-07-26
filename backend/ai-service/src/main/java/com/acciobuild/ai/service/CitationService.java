package com.acciobuild.ai.service;

import com.acciobuild.ai.dto.CitationDto;
import java.util.List;
import java.util.UUID;

/**
 * Service Contract for managing citation annotations for messages.
 */
public interface CitationService {
    List<CitationDto> getCitationsForMessage(UUID messageId);
    void recordCitations(UUID messageId, List<CitationDto> citations);
}
