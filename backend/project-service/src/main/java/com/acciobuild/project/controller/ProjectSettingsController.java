package com.acciobuild.project.controller;

import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.project.dto.ProjectSettingsDto;
import com.acciobuild.project.enums.ProjectFeatureFlag;
import com.acciobuild.project.service.ProjectFeatureFlagService;
import com.acciobuild.project.service.ProjectSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller managing project threshold configurations and feature flags capabilities.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Project Settings Console", description = "Endpoints managing branch defaults, storage limits, and static analysis triggers within a project workspace.")
@SecurityRequirement(name = "bearerAuth")
public class ProjectSettingsController {

    private final ProjectSettingsService settingsService;
    private final ProjectFeatureFlagService featureFlagService;

    @GetMapping("/api/v1/projects/{projectId}/settings")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get Project Settings", description = "Retrieves configurations defined for a project workspace.")
    public ResponseEntity<ApiResponse<ProjectSettingsDto>> getSettings(@PathVariable UUID projectId) {
        log.info("REST request to fetch settings parameters for project ID: {}", projectId);
        ApiResponse<ProjectSettingsDto> response = settingsService.getSettings(projectId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PutMapping("/api/v1/projects/{projectId}/settings")
    @PreAuthorize("hasAnyRole('OWNER', 'MAINTAINER')")
    @Operation(summary = "Update Project Settings", description = "Modifies branch configurations, AI assistants, and documentation parameters (Maintainer/Owner only).")
    public ResponseEntity<ApiResponse<ProjectSettingsDto>> updateSettings(
            @PathVariable UUID projectId,
            @Valid @RequestBody ProjectSettingsDto request) {
        log.info("REST request to update settings limits for project ID: {}", projectId);
        ApiResponse<ProjectSettingsDto> response = settingsService.updateSettings(projectId, request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/api/v1/projects/{projectId}/configuration")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get Project Config", description = "Retrieves the complete set of system configuration parameters for a workspace project.")
    public ResponseEntity<ApiResponse<ProjectSettingsDto>> getConfiguration(@PathVariable UUID projectId) {
        log.info("REST request to retrieve system configuration for project ID: {}", projectId);
        ApiResponse<ProjectSettingsDto> response = settingsService.getSettings(projectId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PutMapping("/api/v1/projects/{projectId}/configuration")
    @PreAuthorize("hasAnyRole('OWNER', 'MAINTAINER')")
    @Operation(summary = "Update Project Config", description = "Modifies system configurations and settings limits thresholds for a project workspace.")
    public ResponseEntity<ApiResponse<ProjectSettingsDto>> updateConfiguration(
            @PathVariable UUID projectId,
            @Valid @RequestBody ProjectSettingsDto request) {
        log.info("REST request to alter configuration threshold values for project ID: {}", projectId);
        ApiResponse<ProjectSettingsDto> response = settingsService.updateSettings(projectId, request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/api/v1/projects/{projectId}/features")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get Feature Flags Summary", description = "Retrieves all feature flags active status codes mapped for the workspace.")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> getFeatures(@PathVariable UUID projectId) {
        log.info("REST request to view project feature flag status map for ID: {}", projectId);
        ApiResponse<Map<String, Boolean>> response = featureFlagService.getFeatures(projectId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PutMapping("/api/v1/projects/{projectId}/features/{feature}")
    @PreAuthorize("hasAnyRole('OWNER', 'MAINTAINER')")
    @Operation(summary = "Toggle Feature Flag", description = "Enables or disables a specific project capability feature flag.")
    public ResponseEntity<ApiResponse<Void>> toggleFeature(
            @PathVariable UUID projectId,
            @PathVariable String feature,
            @RequestParam boolean enabled) {
        log.info("REST request to toggle feature: {} to {} inside project ID: {}", feature, enabled, projectId);
        ProjectFeatureFlag flag = ProjectFeatureFlag.valueOf(feature.toUpperCase().trim());
        ApiResponse<Void> response = featureFlagService.toggleFeature(projectId, flag, enabled);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}
