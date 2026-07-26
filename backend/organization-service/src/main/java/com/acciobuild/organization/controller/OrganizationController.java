package com.acciobuild.organization.controller;

import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.common.dto.PagedResponse;
import com.acciobuild.common.security.SecurityContextHelper;
import com.acciobuild.organization.dto.OrganizationDto;
import com.acciobuild.organization.dto.OrganizationRequest;
import com.acciobuild.organization.service.OrganizationService;
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
 * REST controller exposing endpoints to register, update, and manage tenant organizations.
 */
@RestController
@RequestMapping("/api/v1/organizations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Organization Management Console", description = "Endpoints exposing CRUD operations for SaaS organization tenant profiles.")
@SecurityRequirement(name = "bearerAuth")
public class OrganizationController {

    private final OrganizationService organizationService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create Organization", description = "Registers a new tenant organization profile and provisions default settings.")
    public ResponseEntity<ApiResponse<OrganizationDto>> createOrganization(@Valid @RequestBody OrganizationRequest request) {
        UUID creatorId = SecurityContextHelper.getCurrentUserId();
        log.info("REST request to register organization: {} by creator ID: {}", request.getOrganizationName(), creatorId);
        ApiResponse<OrganizationDto> response = organizationService.createOrganization(request, creatorId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PutMapping("/{organizationId}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Update Organization", description = "Updates organization profile metadata fields.")
    public ResponseEntity<ApiResponse<OrganizationDto>> updateOrganization(
            @PathVariable UUID organizationId,
            @Valid @RequestBody OrganizationRequest request) {
        log.info("REST request to update organization ID: {}", organizationId);
        ApiResponse<OrganizationDto> response = organizationService.updateOrganization(organizationId, request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @DeleteMapping("/{organizationId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or (hasRole('OWNER') and !#permanent)")
    @Operation(summary = "Delete Organization", description = "Soft deletes organization by default. If permanent=true, performs database purge (Super Admin only).")
    public ResponseEntity<ApiResponse<Void>> deleteOrganization(
            @PathVariable UUID organizationId,
            @RequestParam(defaultValue = "false") boolean permanent) {
        if (permanent) {
            log.warn("REST request to permanently delete organization ID: {}", organizationId);
            ApiResponse<Void> response = organizationService.permanentDelete(organizationId);
            return ResponseEntity.status(response.getStatus()).body(response);
        } else {
            log.warn("REST request to soft delete organization ID: {}", organizationId);
            ApiResponse<Void> response = organizationService.deleteOrganization(organizationId);
            return ResponseEntity.status(response.getStatus()).body(response);
        }
    }

    @PostMapping("/{organizationId}/activate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Activate Organization", description = "Restores organization status to ACTIVE.")
    public ResponseEntity<ApiResponse<OrganizationDto>> activateOrganization(@PathVariable UUID organizationId) {
        log.info("REST request to activate organization: {}", organizationId);
        ApiResponse<OrganizationDto> response = organizationService.activateOrganization(organizationId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/{organizationId}/suspend")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Suspend Organization", description = "Suspends organization (blocks project creations and member invitations).")
    public ResponseEntity<ApiResponse<OrganizationDto>> suspendOrganization(@PathVariable UUID organizationId) {
        log.warn("REST request to suspend organization: {}", organizationId);
        ApiResponse<OrganizationDto> response = organizationService.suspendOrganization(organizationId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/{organizationId}/restore")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Restore Organization", description = "Restores a soft-deleted organization back to active state.")
    public ResponseEntity<ApiResponse<OrganizationDto>> restoreOrganization(@PathVariable UUID organizationId) {
        log.info("REST request to restore organization: {}", organizationId);
        ApiResponse<OrganizationDto> response = organizationService.restoreOrganization(organizationId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/{organizationId}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MEMBER', 'VIEWER')")
    @Operation(summary = "Fetch Organization Details", description = "Retrieves profile parameters for a specific organization.")
    public ResponseEntity<ApiResponse<OrganizationDto>> getOrganizationById(@PathVariable UUID organizationId) {
        log.info("REST request to fetch organization details for ID: {}", organizationId);
        ApiResponse<OrganizationDto> response = organizationService.getOrganizationById(organizationId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}
