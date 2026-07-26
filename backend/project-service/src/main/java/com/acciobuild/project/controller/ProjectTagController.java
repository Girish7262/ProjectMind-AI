package com.acciobuild.project.controller;

import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.project.dto.ProjectTagDto;
import com.acciobuild.project.service.ProjectTagService;
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
 * REST controller managing tag assignments for workspace projects.
 */
@RestController
@RequestMapping("/api/v1/projects/{projectId}/tags")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Project Tag Console", description = "Endpoints exposing create, remove, and list mappings for categorizing workspace projects.")
@SecurityRequirement(name = "bearerAuth")
public class ProjectTagController {

    private final ProjectTagService tagService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List Project Tags", description = "Retrieves all custom tags associated with the project.")
    public ResponseEntity<ApiResponse<List<ProjectTagDto>>> getTags(@PathVariable UUID projectId) {
        log.info("REST request to list tags for project ID: {}", projectId);
        ApiResponse<List<ProjectTagDto>> response = tagService.getTags(projectId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'MAINTAINER')")
    @Operation(summary = "Add Tag", description = "Maps a new categorization tag onto the project workspace.")
    public ResponseEntity<ApiResponse<ProjectTagDto>> addTag(
            @PathVariable UUID projectId,
            @Valid @RequestBody ProjectTagDto request) {
        log.info("REST request to define tag: {} inside project ID: {}", request.getTagName(), projectId);
        ApiResponse<ProjectTagDto> response = tagService.addTag(projectId, request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @DeleteMapping("/{tagId}")
    @PreAuthorize("hasAnyRole('OWNER', 'MAINTAINER')")
    @Operation(summary = "Remove Tag", description = "Revokes custom tag assignment from the project workspace.")
    public ResponseEntity<ApiResponse<Void>> removeTag(
            @PathVariable UUID projectId,
            @PathVariable UUID tagId) {
        log.warn("REST request to delete tag ID: {} from project ID: {}", tagId, projectId);
        ApiResponse<Void> response = tagService.removeTag(tagId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}
