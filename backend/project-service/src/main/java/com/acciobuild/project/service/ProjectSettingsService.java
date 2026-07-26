package com.acciobuild.project.service;

import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.project.dto.ProjectSettingsDto;
import java.util.UUID;

/**
 * Service interface managing project settings, branch defaults, and analysis tools toggles.
 */
public interface ProjectSettingsService {

    /**
     * Fetches operational settings for a project.
     */
    ApiResponse<ProjectSettingsDto> getSettings(UUID projectId);

    /**
     * Updates settings limits for a project.
     */
    ApiResponse<ProjectSettingsDto> updateSettings(UUID projectId, ProjectSettingsDto dto);
}
