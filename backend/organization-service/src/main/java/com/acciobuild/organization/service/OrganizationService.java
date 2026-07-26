package com.acciobuild.organization.service;

import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.organization.dto.OrganizationDto;
import com.acciobuild.organization.dto.OrganizationRequest;
import java.util.UUID;

/**
 * Service interface outlining Organization aggregate administration contracts.
 */
public interface OrganizationService {

    /**
     * Creates a new organization profile (tenant) along with its initial default settings.
     */
    ApiResponse<OrganizationDto> createOrganization(OrganizationRequest request, UUID creatorUserId);

    /**
     * Updates organization profile metadata.
     */
    ApiResponse<OrganizationDto> updateOrganization(UUID organizationId, OrganizationRequest request);

    /**
     * Deletes an organization by its ID (marks it as deleted for audit/compliance safety).
     */
    ApiResponse<Void> deleteOrganization(UUID organizationId);

    /**
     * Resolves organization profile details by unique ID.
     */
    ApiResponse<OrganizationDto> getOrganizationById(UUID organizationId);

    /**
     * Resolves organization profile details by unique code identifier.
     */
    ApiResponse<OrganizationDto> getOrganizationByCode(String organizationCode);

    /**
     * Activates a suspended or archived organization.
     */
    ApiResponse<OrganizationDto> activateOrganization(UUID organizationId);

    /**
     * Suspends an organization, blocking project creations and member invitations.
     */
    ApiResponse<OrganizationDto> suspendOrganization(UUID organizationId);

    /**
     * Archives an organization, making it read-only.
     */
    ApiResponse<OrganizationDto> archiveOrganization(UUID organizationId);

    /**
     * Restores a soft-deleted organization.
     */
    ApiResponse<OrganizationDto> restoreOrganization(UUID organizationId);

    /**
     * Permanently deletes an organization from the database (Admin only).
     */
    ApiResponse<Void> permanentDelete(UUID organizationId);
}
