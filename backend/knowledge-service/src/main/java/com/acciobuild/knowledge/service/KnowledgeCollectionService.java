package com.acciobuild.knowledge.service;

import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.knowledge.dto.KnowledgeCollectionDto;
import java.util.List;
import java.util.UUID;

/**
 * Service contract managing document collections mappings.
 */
public interface KnowledgeCollectionService {

    /**
     * Registers a new document collection map.
     */
    ApiResponse<KnowledgeCollectionDto> createCollection(KnowledgeCollectionDto dto);

    /**
     * Lists collections registered inside a specific project.
     */
    ApiResponse<List<KnowledgeCollectionDto>> getCollections(UUID projectId);
}
