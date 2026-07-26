package com.acciobuild.knowledge.service;

import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.knowledge.dto.KnowledgeCategoryDto;
import java.util.List;
import java.util.UUID;

/**
 * Service contract managing document categories configurations.
 */
public interface KnowledgeCategoryService {

    /**
     * Registers a new document category classifier.
     */
    ApiResponse<KnowledgeCategoryDto> createCategory(KnowledgeCategoryDto dto);

    /**
     * Lists categories defined inside a specific project.
     */
    ApiResponse<List<KnowledgeCategoryDto>> getCategories(UUID projectId);
}
