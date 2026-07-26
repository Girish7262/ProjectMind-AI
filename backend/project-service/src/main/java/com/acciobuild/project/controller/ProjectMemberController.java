package com.acciobuild.project.controller;

import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.project.dto.AddMemberRequest;
import com.acciobuild.project.dto.ProjectMemberDto;
import com.acciobuild.project.dto.UpdateRoleRequest;
import com.acciobuild.project.service.ProjectMemberService;
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
 * REST controller managing project collaborator membership and role access limits.
 */
@RestController
@RequestMapping("/api/v1/projects/{projectId}/members")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Project Member Console", description = "Endpoints managing collaborator enrollment status and role permissions mappings.")
@SecurityRequirement(name = "bearerAuth")
public class ProjectMemberController {

    private final ProjectMemberService memberService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List Project Collaborators", description = "Retrieves all collaborators currently assigned to a project workspace.")
    public ResponseEntity<ApiResponse<List<ProjectMemberDto>>> getMembers(@PathVariable UUID projectId) {
        log.info("REST request to list members for project ID: {}", projectId);
        ApiResponse<List<ProjectMemberDto>> response = memberService.getMembers(projectId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'MAINTAINER')")
    @Operation(summary = "Add Collaborator", description = "Enrolls a user into the project workspace (verifies user existence in Auth Service via Feign).")
    public ResponseEntity<ApiResponse<ProjectMemberDto>> addMember(
            @PathVariable UUID projectId,
            @Valid @RequestBody AddMemberRequest request) {
        log.info("REST request to enroll member user ID: {} to project ID: {}", request.getUserId(), projectId);
        ApiResponse<ProjectMemberDto> response = memberService.addMember(projectId, request.getUserId(), request.getRole());
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PutMapping("/{memberId}")
    @PreAuthorize("hasAnyRole('OWNER', 'MAINTAINER')")
    @Operation(summary = "Update Collaborator Role", description = "Changes project role configuration (MAINTAINER, DEVELOPER, VIEWER). Safeguards sole maintainer.")
    public ResponseEntity<ApiResponse<ProjectMemberDto>> updateMemberRole(
            @PathVariable UUID projectId,
            @PathVariable UUID memberId,
            @Valid @RequestBody UpdateRoleRequest request) {
        log.info("REST request to modify role for user ID: {} to {} inside project ID: {}", memberId, request.getRole(), projectId);
        ApiResponse<ProjectMemberDto> response = memberService.updateMemberRole(projectId, memberId, request.getRole());
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @DeleteMapping("/{memberId}")
    @PreAuthorize("hasAnyRole('OWNER', 'MAINTAINER')")
    @Operation(summary = "Remove Collaborator", description = "Revokes project access. Safeguards sole maintainer.")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable UUID projectId,
            @PathVariable UUID memberId) {
        log.warn("REST request to remove collaborator user ID: {} from project ID: {}", memberId, projectId);
        ApiResponse<Void> response = memberService.removeMember(projectId, memberId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/{memberId}/transfer-owner")
    @PreAuthorize("hasAnyRole('OWNER', 'MAINTAINER')")
    @Operation(summary = "Transfer Project Ownership", description = "Promotes a member user to MAINTAINER (demotes the old maintainer to DEVELOPER).")
    public ResponseEntity<ApiResponse<ProjectMemberDto>> transferOwner(
            @PathVariable UUID projectId,
            @PathVariable UUID memberId) {
        log.info("REST request to transfer project ownership to user ID: {} inside project ID: {}", memberId, projectId);
        ApiResponse<ProjectMemberDto> response = memberService.updateMemberRole(projectId, memberId, "MAINTAINER");
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}
