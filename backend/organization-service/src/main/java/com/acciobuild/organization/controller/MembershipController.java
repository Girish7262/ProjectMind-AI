package com.acciobuild.organization.controller;

import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.common.dto.PagedResponse;
import com.acciobuild.organization.dto.AddMemberRequest;
import com.acciobuild.organization.dto.MemberDto;
import com.acciobuild.organization.dto.UpdateRoleRequest;
import com.acciobuild.organization.enums.MemberRole;
import com.acciobuild.organization.enums.MemberStatus;
import com.acciobuild.organization.service.MembershipService;
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
 * REST controller managing user membership enrollment, deactivations, and ownership transfer.
 */
@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/members")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Membership Management Console", description = "Endpoints managing user roles assignments, activations, and ownership transfers within an organization.")
@SecurityRequirement(name = "bearerAuth")
public class MembershipController {

    private final MembershipService membershipService;

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MEMBER', 'VIEWER')")
    @Operation(summary = "List Organization Members", description = "Paginates and lists members currently associated with the organization.")
    public ResponseEntity<ApiResponse<PagedResponse<MemberDto>>> getMembers(
            @PathVariable UUID organizationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "joinedAt") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction) {
        log.info("REST request to list members for organization ID: {}", organizationId);
        ApiResponse<PagedResponse<MemberDto>> response = membershipService.getMembersPaged(
                organizationId, page, size, sortBy, direction);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Add Member Directly", description = "Enrolls a user into organization membership (performs external user validations via Feign).")
    public ResponseEntity<ApiResponse<MemberDto>> addMember(
            @PathVariable UUID organizationId,
            @Valid @RequestBody AddMemberRequest request) {
        log.info("REST request to enroll member user ID: {} to organization ID: {}", request.getUserId(), organizationId);
        MemberRole roleEnum = MemberRole.valueOf(request.getRole().toUpperCase().trim());
        ApiResponse<MemberDto> response = membershipService.addMember(organizationId, request.getUserId(), roleEnum);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PutMapping("/{memberId}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Update Member Role", description = "Changes membership role assignment (e.g. to ADMIN, MEMBER). Blocks removing sole owner.")
    public ResponseEntity<ApiResponse<MemberDto>> updateMemberRole(
            @PathVariable UUID organizationId,
            @PathVariable UUID memberId,
            @Valid @RequestBody UpdateRoleRequest request) {
        log.info("REST request to change role for user ID: {} to {}", memberId, request.getRole());
        MemberRole roleEnum = MemberRole.valueOf(request.getRole().toUpperCase().trim());
        ApiResponse<MemberDto> response = membershipService.updateMemberRole(organizationId, memberId, roleEnum);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @DeleteMapping("/{memberId}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Remove Member", description = "Revokes user organization access. Blocks removing the organization owner.")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable UUID organizationId,
            @PathVariable UUID memberId) {
        log.warn("REST request to remove member user ID: {} from organization ID: {}", memberId, organizationId);
        ApiResponse<Void> response = membershipService.removeMember(organizationId, memberId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/{memberId}/transfer-owner")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Transfer Ownership", description = "Promotes a member user to OWNER, demoting the current owner to ADMIN.")
    public ResponseEntity<ApiResponse<MemberDto>> transferOwner(
            @PathVariable UUID organizationId,
            @PathVariable UUID memberId) {
        log.info("REST request to transfer ownership to user ID: {} in organization ID: {}", memberId, organizationId);
        ApiResponse<MemberDto> response = membershipService.updateMemberRole(organizationId, memberId, MemberRole.OWNER);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/{memberId}/activate")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Reactivate Member", description = "Restores suspended membership access.")
    public ResponseEntity<ApiResponse<MemberDto>> activateMember(
            @PathVariable UUID organizationId,
            @PathVariable UUID memberId) {
        log.info("REST request to activate membership for user ID: {}", memberId);
        ApiResponse<MemberDto> response = membershipService.updateMemberStatus(organizationId, memberId, MemberStatus.ACTIVE);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/{memberId}/deactivate")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Suspend/Deactivate Member", description = "Temporarily suspends membership access.")
    public ResponseEntity<ApiResponse<MemberDto>> deactivateMember(
            @PathVariable UUID organizationId,
            @PathVariable UUID memberId) {
        log.warn("REST request to suspend membership for user ID: {}", memberId);
        ApiResponse<MemberDto> response = membershipService.updateMemberStatus(organizationId, memberId, MemberStatus.BLOCKED);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}
