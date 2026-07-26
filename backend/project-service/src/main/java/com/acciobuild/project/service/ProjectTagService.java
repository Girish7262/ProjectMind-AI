package com.acciobuild.project.service;

import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.project.dto.ProjectTagDto;
import java.util.List;
import java.util.UUID;

/**
 * Service interface managing tags classification mappings.
 */
public interface ProjectTagService {

    /**
     * Adds a categorization tag to a project.
     */
    ApiResponse<ProjectTagDto> addTag(UUID projectId, ProjectTagDto dto);

    /**
     * Removes a tag from a project.
     */
    ApiResponse<Void> removeTag(UUID tagId);

    /**
     * Lists all tags defined for a project.
     */
    ApiResponse<List<ProjectTagDto>> getTags(UUID projectId);
}
