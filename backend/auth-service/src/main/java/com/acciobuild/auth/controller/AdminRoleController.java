package com.acciobuild.auth.controller;

import com.acciobuild.auth.dto.*;
import com.acciobuild.auth.service.PermissionAssignmentService;
import com.acciobuild.auth.service.PermissionManagementService;
import com.acciobuild.auth.service.RoleAssignmentService;
import com.acciobuild.auth.service.RoleManagementService;
import com.acciobuild.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
 * REST controller exposing role management, permission assignment, and user role mapping endpoints.
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "RBAC Console", description = "Endpoints managing Roles definitions, user mappings, and Permission assignments.")
public class AdminRoleController {

    private final RoleManagementService roleManagementService;
    private final PermissionManagementService permissionManagementService;
    private final RoleAssignmentService roleAssignmentService;
    private final PermissionAssignmentService permissionAssignmentService;

    @PostMapping("/roles")
    @PreAuthorize("hasPermission(null, 'ROLE_MANAGE') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Create Role", description = "Creates a new Role and binds optional permission IDs.")
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(@Valid @RequestBody RoleRequest request) {
        log.info("REST request to create role: {}", request.getName());
        ApiResponse<RoleResponse> response = roleManagementService.createRole(request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PutMapping("/roles/{id}")
    @PreAuthorize("hasPermission(null, 'ROLE_MANAGE') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Update Role", description = "Modifies description, status, and permissions for a role.")
    public ResponseEntity<ApiResponse<RoleResponse>> updateRole(
            @PathVariable UUID id, 
            @Valid @RequestBody RoleRequest request) {
        log.info("REST request to update role ID: {}", id);
        ApiResponse<RoleResponse> response = roleManagementService.updateRole(id, request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @DeleteMapping("/roles/{id}")
    @PreAuthorize("hasPermission(null, 'ROLE_MANAGE') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Delete Role", description = "Deletes a role definition (prevents deleting system roles).")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable UUID id) {
        log.info("REST request to delete role ID: {}", id);
        ApiResponse<Void> response = roleManagementService.deleteRole(id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/roles")
    @PreAuthorize("hasPermission(null, 'USER_VIEW') or hasPermission(null, 'ROLE_MANAGE') or hasRole('SUPER_ADMIN') or hasRole('ORG_ADMIN')")
    @Operation(summary = "List/Search Roles", description = "Retrieves all active roles or filters based on search queries.")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getRoles(@RequestParam(required = false) String search) {
        log.info("REST request to fetch roles list. Search filter: {}", search);
        ApiResponse<List<RoleResponse>> response = (search != null && !search.trim().isEmpty())
                ? roleManagementService.searchRoles(search)
                : roleManagementService.findRoles();
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/permissions")
    @PreAuthorize("hasPermission(null, 'USER_VIEW') or hasPermission(null, 'ROLE_MANAGE') or hasRole('SUPER_ADMIN') or hasRole('ORG_ADMIN')")
    @Operation(summary = "List Permissions", description = "Retrieves all available fine-grained system permissions.")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getPermissions(@RequestParam(required = false) String module) {
        log.info("REST request to list permissions. Module filter: {}", module);
        ApiResponse<List<PermissionResponse>> response = (module != null && !module.trim().isEmpty())
                ? permissionManagementService.getPermissionsByModule(module)
                : permissionManagementService.getPermissions();
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/users/{id}/roles")
    @PreAuthorize("hasPermission(null, 'ROLE_MANAGE') or hasRole('SUPER_ADMIN') or hasRole('ORG_ADMIN')")
    @Operation(summary = "Replace User Roles", description = "Substitutes the user's current roles with the requested set (enforces hierarchy block).")
    public ResponseEntity<ApiResponse<UserRolesResponse>> replaceUserRoles(
            @PathVariable UUID id, 
            @Valid @RequestBody AssignRoleRequest request) {
        log.info("REST request to substitute roles for user ID: {} to {}", id, request.getRoleNames());
        ApiResponse<UserRolesResponse> response = roleAssignmentService.replaceUserRoles(id, request.getRoleNames());
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @DeleteMapping("/users/{id}/roles/{roleId}")
    @PreAuthorize("hasPermission(null, 'ROLE_MANAGE') or hasRole('SUPER_ADMIN') or hasRole('ORG_ADMIN')")
    @Operation(summary = "Remove User Role", description = "Revokes a specific role assignment from a user.")
    public ResponseEntity<ApiResponse<UserRolesResponse>> removeUserRole(
            @PathVariable UUID id, 
            @PathVariable UUID roleId) {
        log.info("REST request to remove role ID {} from user ID: {}", roleId, id);
        ApiResponse<UserRolesResponse> response = roleAssignmentService.removeRoleFromUser(id, roleId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/roles/{roleId}/permissions/{permissionId}")
    @PreAuthorize("hasPermission(null, 'PERMISSION_MANAGE') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Assign Permission to Role", description = "Maps a permission directly to a role.")
    public ResponseEntity<ApiResponse<RoleResponse>> assignPermissionToRole(
            @PathVariable UUID roleId, 
            @PathVariable UUID permissionId) {
        log.info("REST request to map permission ID {} to role ID {}", permissionId, roleId);
        ApiResponse<RoleResponse> response = permissionAssignmentService.assignPermissionToRole(roleId, permissionId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @DeleteMapping("/roles/{roleId}/permissions/{permissionId}")
    @PreAuthorize("hasPermission(null, 'PERMISSION_MANAGE') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Remove Permission from Role", description = "Unmaps a permission from a role.")
    public ResponseEntity<ApiResponse<RoleResponse>> removePermissionFromRole(
            @PathVariable UUID roleId, 
            @PathVariable UUID permissionId) {
        log.info("REST request to unmap permission ID {} from role ID {}", permissionId, roleId);
        ApiResponse<RoleResponse> response = permissionAssignmentService.removePermissionFromRole(roleId, permissionId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}
