package com.acciobuild.organization.controller;

import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.common.dto.PagedResponse;
import com.acciobuild.common.security.SecurityContextHelper;
import com.acciobuild.organization.dto.InvitationDto;
import com.acciobuild.organization.dto.InvitationRequest;
import com.acciobuild.organization.service.InvitationService;
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
 * REST controller exposing endpoints to send, list, accept, reject, and resend user invitations.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Invitation Management Console", description = "Endpoints handling member invitations and enrollment acceptance flows.")
@SecurityRequirement(name = "bearerAuth")
public class InvitationController {

    private final InvitationService invitationService;

    @PostMapping("/api/v1/organizations/{organizationId}/invitations")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Send Invitation", description = "Generates a secure invitation token for an invitee's email address.")
    public ResponseEntity<ApiResponse<InvitationDto>> inviteMember(
            @PathVariable UUID organizationId,
            @Valid @RequestBody InvitationRequest request) {
        UUID inviterId = SecurityContextHelper.getCurrentUserId();
        log.info("REST request to invite email: {} to organization: {}", request.getEmail(), organizationId);
        ApiResponse<InvitationDto> response = invitationService.createInvitation(organizationId, request, inviterId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/api/v1/organizations/{organizationId}/invitations")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "List Organization Invitations", description = "Retrieves all invitations associated with the organization.")
    public ResponseEntity<ApiResponse<PagedResponse<InvitationDto>>> getInvitations(
            @PathVariable UUID organizationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "expiresAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        log.info("REST request to list invitations for organization ID: {}", organizationId);
        ApiResponse<PagedResponse<InvitationDto>> response = invitationService.getInvitationsPaged(
                organizationId, page, size, sortBy, direction);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/api/v1/invitations/accept")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Accept Invitation", description = "Consumes invitation token and registers user into the organization membership.")
    public ResponseEntity<ApiResponse<Void>> acceptInvitation(@RequestParam String token) {
        UUID userId = SecurityContextHelper.getCurrentUserId();
        log.info("REST request to accept invitation token by user ID: {}", userId);
        ApiResponse<Void> response = invitationService.acceptInvitation(token, userId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/api/v1/invitations/reject")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Reject Invitation", description = "Rejects and revokes an active invitation token.")
    public ResponseEntity<ApiResponse<Void>> rejectInvitation(@RequestParam String token) {
        log.info("REST request to reject invitation token");
        ApiResponse<InvitationDto> inviteRes = invitationService.getInvitationByToken(token);
        ApiResponse<Void> response = invitationService.revokeInvitation(inviteRes.getData().getId());
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/api/v1/invitations/resend")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Resend Invitation", description = "Revokes existing pending invitation token and issues a new invitation request.")
    public ResponseEntity<ApiResponse<InvitationDto>> resendInvitation(@RequestParam String token) {
        log.info("REST request to resend invitation token");
        ApiResponse<InvitationDto> oldInvite = invitationService.getInvitationByToken(token);
        
        // Revoke old invitation
        invitationService.revokeInvitation(oldInvite.getData().getId());
        
        // Create new invitation request
        InvitationRequest request = new InvitationRequest(oldInvite.getData().getEmail());
        UUID inviterId = SecurityContextHelper.getCurrentUserId();
        
        ApiResponse<InvitationDto> response = invitationService.createInvitation(
                oldInvite.getData().getOrganizationId(), request, inviterId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}
