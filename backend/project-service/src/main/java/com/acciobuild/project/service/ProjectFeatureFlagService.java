package com.acciobuild.project.service;

import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.project.enums.ProjectFeatureFlag;
import java.util.Map;
import java.util.UUID;

/**
 * Service contract managing project-level capability feature flags.
 */
public interface ProjectFeatureFlagService {

    /**
     * Checks if a specific feature flag is currently active for the project.
     */
    boolean isFeatureEnabled(UUID projectId, ProjectFeatureFlag flag);

    /**
     * Toggles a feature flag status. Enables/disables the capability.
     */
    ApiResponse<Void> toggleFeature(UUID projectId, ProjectFeatureFlag flag, boolean enabled);

    /**
     * Retrieves all feature flags and their active states for a project.
     */
    ApiResponse<Map<String, Boolean>> getFeatures(UUID projectId);
}
