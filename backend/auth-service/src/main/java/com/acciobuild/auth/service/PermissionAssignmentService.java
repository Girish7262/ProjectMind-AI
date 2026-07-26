package com.acciobuild.auth.service;

import com.acciobuild.auth.dto.RoleResponse;
import com.acciobuild.common.dto.ApiResponse;
import java.util.UUID;

/**
 * Service interface managing role-permissions bindings mapping details.
 */
public interface PermissionAssignmentService {

    /**
     * Map a permission to a role.
     */
    ApiResponse<RoleResponse> assignPermissionToRole(UUID roleId, UUID permissionId);

    /**
     * Unmap a permission from a role.
     */
    ApiResponse<RoleResponse> removePermissionFromRole(UUID roleId, UUID permissionId);
}
