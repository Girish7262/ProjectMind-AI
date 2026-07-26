package com.acciobuild.auth.service;

import com.acciobuild.auth.entity.User;
import com.acciobuild.auth.dto.UserProfileResponse;
import com.acciobuild.common.dto.ApiResponse;
import java.util.UUID;

/**
 * Service interface managing user profiles, role assignments, and status checks.
 */
public interface UserService {

    /**
     * Retrieves user profile details by ID.
     */
    ApiResponse<UserProfileResponse> getUserProfile(UUID userId);

    /**
     * Assigns a role to a user.
     */
    ApiResponse<Void> assignRole(UUID userId, String roleName);

    /**
     * Removes a role from a user.
     */
    ApiResponse<Void> removeRole(UUID userId, String roleName);

    /**
     * Loads a user record by email.
     */
    User getUserByEmail(String email);
}
