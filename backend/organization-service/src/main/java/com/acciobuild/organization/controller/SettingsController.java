package com.acciobuild.organization.controller;

import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.organization.dto.SettingsDto;
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
import java.util.UUID;

/**
 * REST controller exposing endpoints to fetch and modify organization setting limits.
 */
@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/settings")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Settings Management Console", description = "Endpoints managing organization operational policies, AI feature activations, and maximum member/project settings.")
@SecurityRequirement(name = "bearerAuth")
public class SettingsController {

    private final SettingsService settingsService;

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MEMBER')")
    @Operation(summary = "Get Organization Settings", description = "Retrieves operational settings details for the organization.")
    public ResponseEntity<ApiResponse<SettingsDto>> getSettings(@PathVariable UUID organizationId) {
        log.info("REST request to fetch settings parameters for organization ID: {}", organizationId);
        ApiResponse<SettingsDto> response = settingsService.getSettings(organizationId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Update Organization Settings", description = "Modifies operational rules, AI toggles, and maximum membership/project boundaries.")
    public ResponseEntity<ApiResponse<SettingsDto>> updateSettings(
            @PathVariable UUID organizationId,
            @Valid @RequestBody SettingsDto request) {
        log.info("REST request to update settings limits for organization ID: {}", organizationId);
        ApiResponse<SettingsDto> response = settingsService.updateSettings(organizationId, request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}
