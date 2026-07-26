package com.acciobuild.auth.service;

import com.acciobuild.auth.entity.Permission;
import com.acciobuild.common.dto.ApiResponse;
import java.util.List;
import java.util.UUID;

/**
 * Service interface managing role permissions associations.
 */
public interface PermissionService {

    ApiResponse<Void> assignPermission(UUID roleId, UUID permissionId);

    ApiResponse<Void> removePermission(UUID roleId, UUID permissionId);

    ApiResponse<List<Permission>> getPermissions();
}
