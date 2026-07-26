package com.acciobuild.project.controller;

import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.project.dto.GitRepositoryDto;
import com.acciobuild.project.service.GitRepositoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

/**
 * REST Controller exposing endpoints to manage Git integration repositories.
 */
@RestController
@RequestMapping("/api/v1/projects/{projectId}/repositories")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Project Git Integration Console", description = "Endpoints managing Git connection bindings inside project workspaces.")
@SecurityRequirement(name = "bearerAuth")
public class GitRepositoryController {

    private final GitRepositoryService repositoryService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List Connected Repositories", description = "Retrieves all Git repository connections registered under the project.")
    public ResponseEntity<ApiResponse<List<GitRepositoryDto>>> getRepositories(@PathVariable UUID projectId) {
        log.info("REST request to list repositories for project ID: {}", projectId);
        ApiResponse<List<GitRepositoryDto>> response = repositoryService.getRepositories(projectId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'MAINTAINER')")
    @Operation(summary = "Register Git Repository", description = "Connects a new Git repository (validates quotas limits from ProjectSettings).")
    public ResponseEntity<ApiResponse<GitRepositoryDto>> registerRepository(
            @PathVariable UUID projectId,
            @Valid @RequestBody GitRepositoryDto dto) {
        log.info("REST request to register repository URL: {} inside project ID: {}", dto.getRepositoryUrl(), projectId);
        ApiResponse<GitRepositoryDto> response = repositoryService.registerRepository(projectId, dto);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PutMapping("/{repositoryId}")
    @PreAuthorize("hasAnyRole('OWNER', 'MAINTAINER')")
    @Operation(summary = "Update Repository Metadata", description = "Updates visibility, name, default branch, or archive status parameters of a connected repository.")
    public ResponseEntity<ApiResponse<GitRepositoryDto>> updateRepository(
            @PathVariable UUID projectId,
            @PathVariable UUID repositoryId,
            @Valid @RequestBody GitRepositoryDto dto) {
        log.info("REST request to update repository ID: {} in project ID: {}", repositoryId, projectId);
        ApiResponse<GitRepositoryDto> response = repositoryService.updateRepository(repositoryId, dto);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @DeleteMapping("/{repositoryId}")
    @PreAuthorize("hasAnyRole('OWNER', 'MAINTAINER')")
    @Operation(summary = "Disconnect Git Repository", description = "Removes repository connection configurations from the workspace project.")
    public ResponseEntity<ApiResponse<Void>> deleteRepository(
            @PathVariable UUID projectId,
            @PathVariable UUID repositoryId) {
        log.warn("REST request to disconnect repository ID: {} from project ID: {}", repositoryId, projectId);
        ApiResponse<Void> response = repositoryService.deleteRepository(repositoryId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/{repositoryId}/sync")
    @PreAuthorize("hasAnyRole('OWNER', 'MAINTAINER')")
    @Operation(summary = "Trigger Repository Synchronization", description = "Triggers a full repository commit and metadata sync job.")
    public ResponseEntity<ApiResponse<Void>> syncRepository(
            @PathVariable UUID projectId,
            @PathVariable UUID repositoryId) {
        log.info("REST request to sync repository ID: {} in project ID: {}", repositoryId, projectId);
        ApiResponse<Void> response = repositoryService.syncRepository(repositoryId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}
