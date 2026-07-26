package com.acciobuild.auth.service.impl;

import com.acciobuild.auth.dto.PermissionResponse;
import com.acciobuild.auth.dto.RoleResponse;
import com.acciobuild.auth.dto.UserRolesResponse;
import com.acciobuild.auth.entity.Role;
import com.acciobuild.auth.entity.User;
import com.acciobuild.auth.repository.RoleRepository;
import com.acciobuild.auth.repository.UserRepository;
import com.acciobuild.auth.security.AuthUserDetails;
import com.acciobuild.auth.service.AuditService;
import com.acciobuild.auth.service.RedisTokenService;
import com.acciobuild.auth.service.RefreshTokenService;
import com.acciobuild.auth.service.RoleAssignmentService;
import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.common.exception.BusinessException;
import com.acciobuild.common.exception.ResourceNotFoundException;
import com.acciobuild.common.security.SecurityContextHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service implementation managing user-roles assignments, revocations, and validations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RoleAssignmentServiceImpl implements RoleAssignmentService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuditService auditService;
    private final RedisTokenService redisTokenService;
    private final RefreshTokenService refreshTokenService;
    private final RoleHierarchy roleHierarchy;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<UserRolesResponse> assignRoleToUser(UUID userId, String roleName) {
        log.info("Attempting to assign role {} to user ID: {}", roleName, userId);

        String uppercaseRoleName = roleName.toUpperCase().trim();
        validatePrivilegeEscalation(uppercaseRoleName);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found.", "USER_NOT_FOUND"));

        Role role = roleRepository.findByName(uppercaseRoleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + uppercaseRoleName, "ROLE_NOT_FOUND"));

        if (user.getRoles().contains(role)) {
            return ApiResponse.<UserRolesResponse>builder()
                    .status(200)
                    .message("User already has this role assigned.")
                    .data(mapToUserRolesResponse(user))
                    .build();
        }

        user.getRoles().add(role);
        User savedUser = userRepository.save(user);

        // Terminate user sessions to force refresh/re-authentication
        invalidateUserSessions(userId);

        UUID actorId = SecurityContextHelper.getCurrentUserId();
        auditService.logEvent(actorId, "ROLE_ASSIGNED", "WARN", 
                String.format("Assigned role %s to user %s", uppercaseRoleName, user.getEmail()), getCurrentUserOrgId());

        return ApiResponse.<UserRolesResponse>builder()
                .status(200)
                .message("Role assigned successfully.")
                .data(mapToUserRolesResponse(savedUser))
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<UserRolesResponse> removeRoleFromUser(UUID userId, UUID roleId) {
        log.info("Attempting to remove role ID {} from user ID: {}", roleId, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found.", "USER_NOT_FOUND"));

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found.", "ROLE_NOT_FOUND"));

        validatePrivilegeEscalation(role.getName());

        if (!user.getRoles().contains(role)) {
            throw new BusinessException("User does not have this role assigned.", "ROLE_NOT_ASSIGNED");
        }

        user.getRoles().remove(role);
        User savedUser = userRepository.save(user);

        invalidateUserSessions(userId);

        UUID actorId = SecurityContextHelper.getCurrentUserId();
        auditService.logEvent(actorId, "ROLE_REMOVED", "WARN", 
                String.format("Removed role %s from user %s", role.getName(), user.getEmail()), getCurrentUserOrgId());

        return ApiResponse.<UserRolesResponse>builder()
                .status(200)
                .message("Role removed successfully.")
                .data(mapToUserRolesResponse(savedUser))
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<UserRolesResponse> replaceUserRoles(UUID userId, Set<String> roleNames) {
        log.info("Attempting to replace roles for user ID: {} with set: {}", userId, roleNames);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found.", "USER_NOT_FOUND"));

        Set<Role> newRoles = new HashSet<>();
        for (String name : roleNames) {
            String uppercaseName = name.toUpperCase().trim();
            validatePrivilegeEscalation(uppercaseName);
            Role role = roleRepository.findByName(uppercaseName)
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + uppercaseName, "ROLE_NOT_FOUND"));
            newRoles.add(role);
        }

        user.getRoles().clear();
        user.getRoles().addAll(newRoles);
        User savedUser = userRepository.save(user);

        invalidateUserSessions(userId);

        UUID actorId = SecurityContextHelper.getCurrentUserId();
        auditService.logEvent(actorId, "ROLE_REPLACED", "WARN", 
                String.format("Replaced roles for user %s to %s", user.getEmail(), roleNames), getCurrentUserOrgId());

        return ApiResponse.<UserRolesResponse>builder()
                .status(200)
                .message("User roles replaced successfully.")
                .data(mapToUserRolesResponse(savedUser))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<UserRolesResponse> getUserRoles(UUID userId) {
        log.info("Fetching roles for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found.", "USER_NOT_FOUND"));

        return ApiResponse.<UserRolesResponse>builder()
                .status(200)
                .message("User roles fetched.")
                .data(mapToUserRolesResponse(user))
                .build();
    }

    /**
     * Enforces role hierarchy matching to block privilege escalation.
     */
    private void validatePrivilegeEscalation(String targetRoleName) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new BusinessException("Actor authentication details not found.", "UNAUTHORIZED");
        }

        // SUPER_ADMIN has global overrides
        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"))) {
            return;
        }

        // Protect SUPER_ADMIN assignment explicitly
        if ("SUPER_ADMIN".equalsIgnoreCase(targetRoleName)) {
            throw new BusinessException("Privilege escalation blocked: Only SUPER_ADMIN users can assign SUPER_ADMIN.", "PRIVILEGE_ESCALATION");
        }

        // Get all reachable roles/permissions from the actor's current authorities
        Collection<? extends GrantedAuthority> reachableAuthorities = roleHierarchy.getReachableGrantedAuthorities(auth.getAuthorities());
        String requiredRole = "ROLE_" + targetRoleName;

        boolean isReachable = reachableAuthorities.stream()
                .anyMatch(a -> a.getAuthority().equals(requiredRole));

        if (!isReachable) {
            log.warn("Privilege escalation block: Actor lacks authorization to manage role: {}", targetRoleName);
            throw new BusinessException("Privilege escalation blocked: You cannot manage a role higher than your own hierarchy level.", "PRIVILEGE_ESCALATION");
        }
    }

    /**
     * Deletes user active cache sessions and revokes refresh tokens.
     */
    private void invalidateUserSessions(UUID userId) {
        try {
            redisTokenService.deleteAllSessions(userId);
            refreshTokenService.revokeAllTokens(userId);
            log.info("Successfully revoked all active sessions for User ID: {}", userId);
        } catch (Exception e) {
            log.error("Failed to revoke sessions for User ID: {}", userId, e);
        }
    }

    /**
     * Maps User entity to UserRolesResponse DTO.
     */
    private UserRolesResponse mapToUserRolesResponse(User user) {
        if (user == null) return null;
        Set<RoleResponse> roleResponses = user.getRoles().stream()
                .map(this::mapToRoleResponse)
                .collect(Collectors.toSet());

        return UserRolesResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .roles(roleResponses)
                .build();
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
