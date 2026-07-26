package com.acciobuild.organization.service;

import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.organization.dto.SettingsDto;
import com.acciobuild.organization.enums.FeatureFlag;
import java.util.Map;
import java.util.UUID;

/**
 * Service interface managing Feature Flags configurations and lookups.
 */
public interface FeatureFlagService {

    /**
     * Checks if a specific feature flag is currently active/enabled for a tenant.
     */
    boolean isFeatureEnabled(UUID organizationId, FeatureFlag feature);

    /**
     * Activates a feature flag for the tenant organization.
     */
    ApiResponse<SettingsDto> enableFeature(UUID organizationId, FeatureFlag feature);

    /**
     * Deactivates a feature flag for the tenant organization.
     */
    ApiResponse<SettingsDto> disableFeature(UUID organizationId, FeatureFlag feature);

    /**
     * Fetches all active feature flags state for the tenant organization.
     */
    ApiResponse<Map<String, Boolean>> getFeatures(UUID organizationId);
}
