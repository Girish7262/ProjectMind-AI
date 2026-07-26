package com.acciobuild.project.service;

import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.project.dto.GitRepositoryDto;
import java.util.List;
import java.util.UUID;

/**
 * Service contract managing connected Git repositories enrollment, status toggles,
 * and sync executions.
 */
public interface GitRepositoryService {

    /**
     * Registers a new Git repository connection.
     */
    ApiResponse<GitRepositoryDto> registerRepository(UUID projectId, GitRepositoryDto dto);

    /**
     * Updates default branch, visibility, or name of a registered repository.
     */
    ApiResponse<GitRepositoryDto> updateRepository(UUID repositoryId, GitRepositoryDto dto);

    /**
     * Soft deletes or removes a repository registration.
     */
    ApiResponse<Void> deleteRepository(UUID repositoryId);

    /**
     * Dispatches a manual or automated synchronization task.
     */
    ApiResponse<Void> syncRepository(UUID repositoryId);

    /**
     * Lists all registered repositories associated with a project.
     */
    ApiResponse<List<GitRepositoryDto>> getRepositories(UUID projectId);
}
