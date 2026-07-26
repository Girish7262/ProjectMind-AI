package com.acciobuild.auth.service;

import com.acciobuild.auth.dto.RoleRequest;
import com.acciobuild.auth.dto.RoleResponse;
import com.acciobuild.common.dto.ApiResponse;
import java.util.List;
import java.util.UUID;

/**
 * Service interface managing role definitions CRUD operations and validations.
 */
public interface RoleManagementService {

    /**
     * Creates a new role and associates the given permissions.
     */
    ApiResponse<RoleResponse> createRole(RoleRequest request);

    /**
     * Updates an existing role's details and metadata.
     */
    ApiResponse<RoleResponse> updateRole(UUID roleId, RoleRequest request);

    /**
     * Deletes a role by ID if it is not protected (e.g. system roles).
     */
    ApiResponse<Void> deleteRole(UUID roleId);

    /**
     * Retrieves a single role by its unique ID.
     */
    ApiResponse<RoleResponse> getRoleById(UUID roleId);

    /**
     * Returns all registered roles.
     */
    ApiResponse<List<RoleResponse>> findRoles();

    /**
     * Search and filter roles based on name query attributes.
     */
    ApiResponse<List<RoleResponse>> searchRoles(String query);
}
