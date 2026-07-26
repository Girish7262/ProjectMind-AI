package com.acciobuild.project.service;

import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.project.dto.ProjectDto;
import com.acciobuild.project.dto.ProjectRequest;
import java.util.UUID;

/**
 * Service interface managing Project aggregate lifecycle and metadata operations.
 */
public interface ProjectService {

    /**
     * Creates a new organization project and provisions default settings boundaries.
     */
    ApiResponse<ProjectDto> createProject(ProjectRequest request, UUID organizationId, UUID creatorUserId);

    /**
     * Updates an existing project profile metadata.
     */
    ApiResponse<ProjectDto> updateProject(UUID projectId, ProjectRequest request);

    /**
     * Soft deletes a project, marking its status as DELETED.
     */
    ApiResponse<Void> deleteProject(UUID projectId);

    /**
     * Fetches details of a project by its primary key ID.
     */
    ApiResponse<ProjectDto> getProjectById(UUID projectId);

    /**
     * Fetches details of a project by its unique code.
     */
    ApiResponse<ProjectDto> getProjectByCode(String projectCode);

    /**
     * Updates the operational status state of a project.
     */
    ApiResponse<ProjectDto> updateProjectStatus(UUID projectId, String status);
}
