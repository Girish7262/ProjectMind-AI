package com.acciobuild.project.controller;

import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.common.security.SecurityContextHelper;
import com.acciobuild.project.dto.ProjectDto;
import com.acciobuild.project.dto.ProjectRequest;
import com.acciobuild.project.multitenancy.TenantContext;
import com.acciobuild.project.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

/**
 * REST controller exposing endpoints to create, update, and manage project lifecycles.
 */
@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Project Management Console", description = "Endpoints exposing CRUD operations for multi-tenant workspace projects.")
@SecurityRequirement(name = "bearerAuth")
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create Project", description = "Registers a new project workspace under the active organization tenant context.")
    public ResponseEntity<ApiResponse<ProjectDto>> createProject(@Valid @RequestBody ProjectRequest request) {
        UUID orgId = TenantContext.getCurrentTenant();
        UUID creatorId = SecurityContextHelper.getCurrentUserId();
        log.info("REST request to register project: {} under organization: {} by creator: {}", request.getProjectName(), orgId, creatorId);
        ApiResponse<ProjectDto> response = projectService.createProject(request, orgId, creatorId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PutMapping("/{projectId}")
    @PreAuthorize("hasAnyRole('OWNER', 'MAINTAINER')")
    @Operation(summary = "Update Project Profile", description = "Updates project code, visibility, and metadata descriptors.")
    public ResponseEntity<ApiResponse<ProjectDto>> updateProject(
            @PathVariable UUID projectId,
            @Valid @RequestBody ProjectRequest request) {
        log.info("REST request to update project ID: {}", projectId);
        ApiResponse<ProjectDto> response = projectService.updateProject(projectId, request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @DeleteMapping("/{projectId}")
    @PreAuthorize("hasAnyRole('OWNER', 'MAINTAINER')")
    @Operation(summary = "Soft Delete Project", description = "Soft deletes project by marking status state as DELETED.")
    public ResponseEntity<ApiResponse<Void>> deleteProject(@PathVariable UUID projectId) {
        log.warn("REST request to soft delete project ID: {}", projectId);
        ApiResponse<Void> response = projectService.deleteProject(projectId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/{projectId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Fetch Project Details", description = "Retrieves profile parameters for a specific project.")
    public ResponseEntity<ApiResponse<ProjectDto>> getProjectById(@PathVariable UUID projectId) {
        log.info("REST request to fetch project details for ID: {}", projectId);
        ApiResponse<ProjectDto> response = projectService.getProjectById(projectId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/{projectId}/archive")
    @PreAuthorize("hasAnyRole('OWNER', 'MAINTAINER')")
    @Operation(summary = "Archive Project", description = "Restricts modify actions by changing project status to ARCHIVED.")
    public ResponseEntity<ApiResponse<ProjectDto>> archiveProject(@PathVariable UUID projectId) {
        log.info("REST request to archive project ID: {}", projectId);
        ApiResponse<ProjectDto> response = projectService.updateProjectStatus(projectId, "ARCHIVED");
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/{projectId}/restore")
    @PreAuthorize("hasAnyRole('OWNER', 'MAINTAINER')")
    @Operation(summary = "Restore Project", description = "Restores archived or planning project status back to ACTIVE.")
    public ResponseEntity<ApiResponse<ProjectDto>> restoreProject(@PathVariable UUID projectId) {
        log.info("REST request to restore project ID: {}", projectId);
        ApiResponse<ProjectDto> response = projectService.updateProjectStatus(projectId, "ACTIVE");
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/{projectId}/activate")
    @PreAuthorize("hasAnyRole('OWNER', 'MAINTAINER')")
    @Operation(summary = "Activate Project", description = "Changes project status to ACTIVE state.")
    public ResponseEntity<ApiResponse<ProjectDto>> activateProject(@PathVariable UUID projectId) {
        log.info("REST request to activate project ID: {}", projectId);
        ApiResponse<ProjectDto> response = projectService.updateProjectStatus(projectId, "ACTIVE");
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}
