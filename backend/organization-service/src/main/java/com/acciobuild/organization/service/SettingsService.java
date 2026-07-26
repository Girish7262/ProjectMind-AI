package com.acciobuild.organization.service;

import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.organization.dto.SettingsDto;
import java.util.UUID;

/**
 * Service interface managing tenant organization settings.
 */
public interface SettingsService {

    /**
     * Retrieves settings details for an organization.
     */
    ApiResponse<SettingsDto> getSettings(UUID organizationId);

    /**
     * Updates settings details for an organization.
     */
    ApiResponse<SettingsDto> updateSettings(UUID organizationId, SettingsDto settings);
}
