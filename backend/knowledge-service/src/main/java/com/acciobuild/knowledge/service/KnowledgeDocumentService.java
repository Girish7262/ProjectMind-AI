package com.acciobuild.knowledge.service;

import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.knowledge.dto.KnowledgeDocumentDto;
import com.acciobuild.knowledge.dto.KnowledgeVersionDto;
import java.util.List;
import java.util.UUID;

/**
 * Service contract managing Knowledge Documents aggregate lifecycles.
 */
public interface KnowledgeDocumentService {

    /**
     * Provisions a new Knowledge Document aggregate.
     */
    ApiResponse<KnowledgeDocumentDto> createDocument(KnowledgeDocumentDto dto, UUID creatorId);

    /**
     * Commits a new immutable version revision state for a document.
     */
    ApiResponse<KnowledgeVersionDto> createVersion(UUID documentId, KnowledgeVersionDto dto, UUID creatorId);

    /**
     * Retrieves document parameters by ID references.
     */
    ApiResponse<KnowledgeDocumentDto> getDocument(UUID id);

    /**
     * Retrieves document parameters by project and slug identifiers.
     */
    ApiResponse<KnowledgeDocumentDto> getDocumentBySlug(UUID projectId, String slug);
}
