package com.acciobuild.auth.service;

import com.acciobuild.auth.dto.PermissionResponse;
import com.acciobuild.common.dto.ApiResponse;
import java.util.List;

/**
 * Service interface managing available system permission queries.
 */
public interface PermissionManagementService {

    /**
     * Lists all registered system permissions.
     */
    ApiResponse<List<PermissionResponse>> getPermissions();

    /**
     * Filter permissions by target functional module.
     */
    ApiResponse<List<PermissionResponse>> getPermissionsByModule(String module);
}
