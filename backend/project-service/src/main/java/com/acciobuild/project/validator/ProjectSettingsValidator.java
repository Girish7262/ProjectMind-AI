package com.acciobuild.project.validator;

import com.acciobuild.project.dto.ProjectSettingsDto;
import com.acciobuild.project.dto.SettingsDto;
import com.acciobuild.project.exception.InvalidProjectOperationException;
import org.springframework.stereotype.Component;

/**
 * Validator verifying that project settings thresholds do not violate
 * active parent organization subscription plans limits.
 */
@Component
public class ProjectSettingsValidator {

    /**
     * Validates project configuration thresholds against parent organization configuration.
     */
    public void validateSettings(ProjectSettingsDto projectSettings, SettingsDto orgSettings) {
        if (projectSettings == null || orgSettings == null) {
            return;
        }

        // 1. Enforce AI Feature availability
        if (projectSettings.isAiEnabled() && !orgSettings.isAiEnabled()) {
            throw new InvalidProjectOperationException("AI feature is disabled at the organization level.");
        }

        // 2. Enforce Project limits do not exceed Organization caps
        if (projectSettings.getMaxRepositories() > orgSettings.getMaxProjects() * 5) {
            throw new InvalidProjectOperationException("Maximum repositories exceeds organization limitations threshold.");
        }

        // 3. Webhook URL format check
        if (projectSettings.getWebhookUrl() != null && !projectSettings.getWebhookUrl().isBlank()) {
            String url = projectSettings.getWebhookUrl();
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                throw new InvalidProjectOperationException("Webhook URL must start with http:// or https://");
            }
        }
    }
}
