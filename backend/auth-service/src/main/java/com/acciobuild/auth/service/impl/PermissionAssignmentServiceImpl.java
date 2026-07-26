package com.acciobuild.auth.service.impl;

import com.acciobuild.auth.dto.PermissionResponse;
import com.acciobuild.auth.dto.RoleResponse;
import com.acciobuild.auth.entity.Permission;
import com.acciobuild.auth.entity.Role;
import com.acciobuild.auth.entity.User;
import com.acciobuild.auth.repository.PermissionRepository;
import com.acciobuild.auth.repository.RoleRepository;
import com.acciobuild.auth.repository.UserRepository;
import com.acciobuild.auth.security.AuthUserDetails;
import com.acciobuild.auth.service.AuditService;
import com.acciobuild.auth.service.PermissionAssignmentService;
import com.acciobuild.auth.service.RedisTokenService;
import com.acciobuild.auth.service.RefreshTokenService;
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
 * Service implementation managing Role and Permission association mappings.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionAssignmentServiceImpl implements PermissionAssignmentService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final RedisTokenService redisTokenService;
    private final RefreshTokenService refreshTokenService;

    // Set of protected system roles whose permissions cannot be dynamically edited
    private static final Set<String> SYSTEM_ROLES = Set.of(
            "SUPER_ADMIN", "ORG_ADMIN", "PROJECT_ADMIN", "DEVELOPER", "REVIEWER", "VIEWER"
    );

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<RoleResponse> assignPermissionToRole(UUID roleId, UUID permissionId) {
        log.info("Attempting to assign permission ID {} to role ID: {}", permissionId, roleId);

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found.", "ROLE_NOT_FOUND"));

        validateSystemRoleProtection(role.getName());

        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found.", "PERMISSION_NOT_FOUND"));

        // Prevent Privilege Escalation: Ensure the actor possesses the permission they are attempting to assign
        validateActorPossessesPermission(permission.getCode());

        if (role.getPermissions().contains(permission)) {
            return ApiResponse.<RoleResponse>builder()
                    .status(200)
                    .message("Role already has this permission assigned.")
                    .data(mapToRoleResponse(role))
                    .build();
        }

        role.getPermissions().add(permission);
        Role savedRole = roleRepository.save(role);

        // Revoke active sessions for all users mapped to this role to force authorities reloading
        invalidateUsersWithRole(role);

        UUID actorId = SecurityContextHelper.getCurrentUserId();
        auditService.logEvent(actorId, "PERMISSION_ADDED", "WARN", 
                String.format("Added permission %s to role %s", permission.getCode(), role.getName()), getCurrentUserOrgId());

        return ApiResponse.<RoleResponse>builder()
                .status(200)
                .message("Permission assigned to role successfully.")
                .data(mapToRoleResponse(savedRole))
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<RoleResponse> removePermissionFromRole(UUID roleId, UUID permissionId) {
        log.info("Attempting to remove permission ID {} from role ID: {}", permissionId, roleId);

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found.", "ROLE_NOT_FOUND"));

        validateSystemRoleProtection(role.getName());

        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found.", "PERMISSION_NOT_FOUND"));

        validateActorPossessesPermission(permission.getCode());

        if (!role.getPermissions().contains(permission)) {
            throw new BusinessException("Permission is not assigned to this role.", "PERMISSION_NOT_ASSIGNED");
        }

        role.getPermissions().remove(permission);
        Role savedRole = roleRepository.save(role);

        invalidateUsersWithRole(role);

        UUID actorId = SecurityContextHelper.getCurrentUserId();
        auditService.logEvent(actorId, "PERMISSION_REMOVED", "WARN", 
                String.format("Removed permission %s from role %s", permission.getCode(), role.getName()), getCurrentUserOrgId());

        return ApiResponse.<RoleResponse>builder()
                .status(200)
                .message("Permission removed from role successfully.")
                .data(mapToRoleResponse(savedRole))
                .build();
    }

    /**
     * Blocks modifying bindings on protected default system roles.
     */
    private void validateSystemRoleProtection(String roleName) {
        if (SYSTEM_ROLES.contains(roleName.toUpperCase())) {
            throw new BusinessException("System roles are protected and their permissions cannot be altered.", "SYSTEM_ROLE_PROTECTED");
        }
    }

    /**
     * Prevents privilege escalation: an actor cannot assign a permission they do not possess.
     */
    private void validateActorPossessesPermission(String permissionCode) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new BusinessException("Actor authentication details not found.", "UNAUTHORIZED");
        }

        // SUPER_ADMIN has global privilege overrides
        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"))) {
            return;
        }

        boolean possessesPermission = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(permissionCode));

        if (!possessesPermission) {
            log.warn("Privilege escalation block: Actor lacks permission code {} to assign/remove it.", permissionCode);
            throw new BusinessException("Privilege escalation blocked: You cannot assign or remove a permission that you do not possess.", "PRIVILEGE_ESCALATION");
        }
    }

    /**
     * Revokes active cache sessions and refresh tokens for all users associated with the modified role.
     */
    private void invalidateUsersWithRole(Role role) {
        try {
            List<User> users = userRepository.findAll().stream()
                    .filter(u -> u.getRoles().contains(role))
                    .collect(Collectors.toList());
            
            for (User user : users) {
                redisTokenService.deleteAllSessions(user.getId());
                refreshTokenService.revokeAllTokens(user.getId());
            }
            log.info("Successfully revoked all active sessions for {} users assigned to role {}", users.size(), role.getName());
        } catch (Exception e) {
            log.error("Failed to revoke user sessions after permission change on role {}", role.getName(), e);
        }
    }

    /**
     * Maps Role Entity to RoleResponse DTO.
     */
    private RoleResponse mapToRoleResponse(Role role) {
        if (role == null) return null;
        Set<PermissionResponse> permissionResponses = role.getPermissions().stream()
                .map(p -> PermissionResponse.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .code(p.getCode())
                        .description(p.getDescription())
                        .module(p.getModule())
                        .build())
                .collect(Collectors.toSet());

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
