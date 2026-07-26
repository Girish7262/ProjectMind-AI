package com.acciobuild.organization.controller;

import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.organization.dto.SettingsDto;
import com.acciobuild.organization.enums.FeatureFlag;
import com.acciobuild.organization.service.FeatureFlagService;
import com.acciobuild.organization.service.SettingsService;
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
 * REST controller exposing SaaS configuration limits and dynamic Feature Flags parameters.
 */
@RestController
@RequestMapping("/api/v1/organizations/{organizationId}")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Configuration & Feature Flags Console", description = "Endpoints configuring active system modules, AI modules, and tenant limits.")
@SecurityRequirement(name = "bearerAuth")
public class ConfigurationController {

    private final SettingsService settingsService;
    private final FeatureFlagService featureFlagService;

    @GetMapping("/configuration")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MEMBER')")
    @Operation(summary = "Get Organization Configuration", description = "Retrieves operational limit configurations for the organization.")
    public ResponseEntity<ApiResponse<SettingsDto>> getConfiguration(@PathVariable UUID organizationId) {
        log.info("REST request to fetch configuration limits for organization ID: {}", organizationId);
        ApiResponse<SettingsDto> response = settingsService.getSettings(organizationId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PutMapping("/configuration")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Update Organization Configuration", description = "Modifies limit configurations (storage limits, file restrictions). Only Owner/Admin.")
    public ResponseEntity<ApiResponse<SettingsDto>> updateConfiguration(
            @PathVariable UUID organizationId,
            @Valid @RequestBody SettingsDto request) {
        log.info("REST request to update configuration limits for organization ID: {}", organizationId);
        ApiResponse<SettingsDto> response = settingsService.updateSettings(organizationId, request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/features")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MEMBER')")
    @Operation(summary = "Get Feature Flags List", description = "Fetches a mapped list of all feature flags activation status.")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> getFeatures(@PathVariable UUID organizationId) {
        log.info("REST request to retrieve feature flags activation list for organization: {}", organizationId);
        ApiResponse<Map<String, Boolean>> response = featureFlagService.getFeatures(organizationId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PutMapping("/features/{feature}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Toggle Feature Flag", description = "Enables or disables a specific feature flag for the organization (Owner/Admin only).")
    public ResponseEntity<ApiResponse<SettingsDto>> toggleFeature(
            @PathVariable UUID organizationId,
            @PathVariable String feature,
            @RequestParam(defaultValue = "true") boolean enabled) {
        log.info("REST request to set feature flag {} to status: {} in organization: {}", feature, enabled, organizationId);
        FeatureFlag flagEnum = FeatureFlag.valueOf(feature.toUpperCase().trim());
        ApiResponse<SettingsDto> response;
        if (enabled) {
            response = featureFlagService.enableFeature(organizationId, flagEnum);
        } else {
            response = featureFlagService.disableFeature(organizationId, flagEnum);
        }
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}
