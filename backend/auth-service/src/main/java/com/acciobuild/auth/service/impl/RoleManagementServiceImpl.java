package com.acciobuild.auth.service.impl;

import com.acciobuild.auth.dto.RoleRequest;
import com.acciobuild.auth.dto.RoleResponse;
import com.acciobuild.auth.dto.PermissionResponse;
import com.acciobuild.auth.entity.Permission;
import com.acciobuild.auth.entity.Role;
import com.acciobuild.auth.repository.PermissionRepository;
import com.acciobuild.auth.repository.RoleRepository;
import com.acciobuild.auth.security.AuthUserDetails;
import com.acciobuild.auth.service.AuditService;
import com.acciobuild.auth.service.RoleManagementService;
import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.common.exception.BusinessException;
import com.acciobuild.common.exception.ResourceNotFoundException;
import com.acciobuild.common.security.SecurityContextHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service implementation managing Role entity CRUD operations, validations, and administrative tasks.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RoleManagementServiceImpl implements RoleManagementService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final AuditService auditService;

    // Set of protected system roles that cannot be deleted or renamed
    private static final Set<String> SYSTEM_ROLES = Set.of(
            "SUPER_ADMIN", "ORG_ADMIN", "PROJECT_ADMIN", "DEVELOPER", "REVIEWER", "VIEWER"
    );

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<RoleResponse> createRole(RoleRequest request) {
        log.info("Attempting to create role: {}", request.getName());

        String uppercaseName = request.getName().toUpperCase().trim();
        if (roleRepository.existsByName(uppercaseName)) {
            throw new BusinessException("Role name already exists: " + uppercaseName, "DUPLICATE_ROLE");
        }

        Role role = new Role();
        role.setName(uppercaseName);
        role.setDescription(request.getDescription());
        role.setStatus(request.isStatus());

        if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
            Set<Permission> permissions = fetchPermissions(request.getPermissionIds());
            role.setPermissions(permissions);
        }

        Role savedRole = roleRepository.save(role);
        
        UUID actorId = SecurityContextHelper.getCurrentUserId();
        auditService.logEvent(actorId, "ROLE_CREATED", "INFO", 
                "Created role " + uppercaseName, getCurrentUserOrgId());

        return ApiResponse.<RoleResponse>builder()
                .status(200)
                .message("Role created successfully.")
                .data(mapToRoleResponse(savedRole))
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<RoleResponse> updateRole(UUID roleId, RoleRequest request) {
        log.info("Attempting to update role ID: {}", roleId);

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found.", "ROLE_NOT_FOUND"));

        // Protect system roles from being modified or renamed
        validateSystemRoleProtection(role.getName());

        String uppercaseName = request.getName().toUpperCase().trim();
        if (!role.getName().equalsIgnoreCase(uppercaseName) && roleRepository.existsByName(uppercaseName)) {
            throw new BusinessException("Role name already exists: " + uppercaseName, "DUPLICATE_ROLE");
        }

        role.setName(uppercaseName);
        role.setDescription(request.getDescription());
        role.setStatus(request.isStatus());

        if (request.getPermissionIds() != null) {
            Set<Permission> permissions = fetchPermissions(request.getPermissionIds());
            role.setPermissions(permissions);
        } else {
            role.getPermissions().clear();
        }

        Role updatedRole = roleRepository.save(role);
        
        UUID actorId = SecurityContextHelper.getCurrentUserId();
        auditService.logEvent(actorId, "ROLE_UPDATED", "WARN", 
                "Updated role details for " + uppercaseName, getCurrentUserOrgId());

        return ApiResponse.<RoleResponse>builder()
                .status(200)
                .message("Role updated successfully.")
                .data(mapToRoleResponse(updatedRole))
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<Void> deleteRole(UUID roleId) {
        log.info("Attempting to delete role ID: {}", roleId);

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found.", "ROLE_NOT_FOUND"));

        validateSystemRoleProtection(role.getName());

        role.getPermissions().clear();
        roleRepository.delete(role);

        UUID actorId = SecurityContextHelper.getCurrentUserId();
        auditService.logEvent(actorId, "ROLE_DELETED", "WARN", 
                "Deleted role " + role.getName(), getCurrentUserOrgId());

        return ApiResponse.<Void>builder()
                .status(200)
                .message("Role deleted successfully.")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<RoleResponse> getRoleById(UUID roleId) {
        log.info("Querying role details for ID: {}", roleId);

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found.", "ROLE_NOT_FOUND"));

        return ApiResponse.<RoleResponse>builder()
                .status(200)
                .message("Role details fetched.")
                .data(mapToRoleResponse(role))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<RoleResponse>> findRoles() {
        log.info("Listing all roles");
        List<Role> roles = roleRepository.findAll();
        
        List<RoleResponse> responses = roles.stream()
                .map(this::mapToRoleResponse)
                .collect(Collectors.toList());

        return ApiResponse.<List<RoleResponse>>builder()
                .status(200)
                .message("All roles fetched successfully.")
                .data(responses)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<RoleResponse>> searchRoles(String query) {
        log.info("Searching roles matching query: {}", query);
        
        List<Role> roles = roleRepository.findAll();
        List<RoleResponse> filteredResponses = roles.stream()
                .filter(r -> r.getName().contains(query.toUpperCase()) || 
                             (r.getDescription() != null && r.getDescription().toLowerCase().contains(query.toLowerCase())))
                .map(this::mapToRoleResponse)
                .collect(Collectors.toList());

        return ApiResponse.<List<RoleResponse>>builder()
                .status(200)
                .message("Roles search results fetched.")
                .data(filteredResponses)
                .build();
    }

    /**
     * Prevents deletion or renaming of system role records.
     */
    private void validateSystemRoleProtection(String roleName) {
        if (SYSTEM_ROLES.contains(roleName.toUpperCase())) {
            throw new BusinessException("System roles are protected and cannot be deleted or modified.", "SYSTEM_ROLE_PROTECTED");
        }
    }

    /**
     * Queries database to resolve a list of Permission entities from their IDs.
     */
    private Set<Permission> fetchPermissions(Set<UUID> ids) {
        Set<Permission> permissions = new HashSet<>();
        for (UUID id : ids) {
            Permission p = permissionRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Permission ID " + id + " not found.", "PERMISSION_NOT_FOUND"));
            permissions.add(p);
        }
        return permissions;
    }

    /**
     * Maps Role Entity to RoleResponse DTO.
     */
    private RoleResponse mapToRoleResponse(Role role) {
        if (role == null) return null;
        Set<PermissionResponse> permissionResponses = new HashSet<>();
        if (role.getPermissions() != null) {
            role.getPermissions().forEach(p -> permissionResponses.add(
                PermissionResponse.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .code(p.getCode())
                        .description(p.getDescription())
                        .module(p.getModule())
                        .build()
            ));
        }
        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .status(role.isStatus())
                .permissions(permissionResponses)
                .build();
    }

    /**
     * Extracts active organization ID from UserDetails context.
     */
    private UUID getCurrentUserOrgId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AuthUserDetails) {
            return ((AuthUserDetails) auth.getPrincipal()).getUser().getOrganizationId();
        }
        return null;
    }
}
