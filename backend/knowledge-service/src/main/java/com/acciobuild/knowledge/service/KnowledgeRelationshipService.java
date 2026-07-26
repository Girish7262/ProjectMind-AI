package com.acciobuild.knowledge.service;

import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.knowledge.dto.KnowledgeRelationshipDto;
import java.util.List;
import java.util.UUID;

/**
 * Service contract managing document relations.
 */
public interface KnowledgeRelationshipService {

    /**
     * Connects two documents.
     */
    ApiResponse<KnowledgeRelationshipDto> linkDocuments(KnowledgeRelationshipDto dto);

    /**
     * Lists relations originating from a source document.
     */
    ApiResponse<List<KnowledgeRelationshipDto>> getRelationships(UUID sourceDocumentId);
}
