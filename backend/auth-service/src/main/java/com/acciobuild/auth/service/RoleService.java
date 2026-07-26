package com.acciobuild.auth.service;

import com.acciobuild.auth.entity.Role;
import com.acciobuild.common.dto.ApiResponse;
import java.util.List;
import java.util.UUID;

/**
 * Service interface managing user roles configurations.
 */
public interface RoleService {

    ApiResponse<Void> assignRole(UUID userId, String roleName);

    ApiResponse<Void> removeRole(UUID userId, String roleName);

    ApiResponse<Role> createRole(String roleName, String description);

    ApiResponse<Role> updateRole(UUID roleId, String description);

    ApiResponse<Void> deleteRole(UUID roleId);

    ApiResponse<List<Role>> findRoles();
}
