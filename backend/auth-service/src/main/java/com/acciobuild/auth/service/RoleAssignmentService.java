package com.acciobuild.auth.service;

import com.acciobuild.auth.dto.UserRolesResponse;
import com.acciobuild.common.dto.ApiResponse;
import java.util.Set;
import java.util.UUID;

/**
 * Service interface managing users roles assignments, revocations, and substitutions.
 */
public interface RoleAssignmentService {

    /**
     * Assigns a role to a user.
     */
    ApiResponse<UserRolesResponse> assignRoleToUser(UUID userId, String roleName);

    /**
     * Removes a role from a user.
     */
    ApiResponse<UserRolesResponse> removeRoleFromUser(UUID userId, UUID roleId);

    /**
     * Replaces a user's entire roles collection with a new set.
     */
    ApiResponse<UserRolesResponse> replaceUserRoles(UUID userId, Set<String> roleNames);

    /**
     * Retrieves the roles currently assigned to a user.
     */
    ApiResponse<UserRolesResponse> getUserRoles(UUID userId);
}
