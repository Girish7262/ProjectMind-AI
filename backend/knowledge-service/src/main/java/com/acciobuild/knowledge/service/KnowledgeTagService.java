package com.acciobuild.knowledge.service;

import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.knowledge.dto.KnowledgeTagDto;
import java.util.List;
import java.util.UUID;

/**
 * Service contract managing document tags classifiers.
 */
public interface KnowledgeTagService {

    /**
     * Registers a new custom tag.
     */
    ApiResponse<KnowledgeTagDto> createTag(KnowledgeTagDto dto);

    /**
     * Lists tags defined inside a specific project.
     */
    ApiResponse<List<KnowledgeTagDto>> getTags(UUID projectId);
}
