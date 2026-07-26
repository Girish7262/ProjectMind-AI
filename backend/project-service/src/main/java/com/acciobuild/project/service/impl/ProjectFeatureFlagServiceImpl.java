package com.acciobuild.project.service.impl;

import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.common.exception.ResourceNotFoundException;
import com.acciobuild.common.util.MdcHelper;
import com.acciobuild.project.domain.event.ProjectFeatureDisabledEvent;
import com.acciobuild.project.domain.event.ProjectFeatureEnabledEvent;
import com.acciobuild.project.domain.model.ProjectSettings;
import com.acciobuild.project.domain.repository.ProjectSettingsRepository;
import com.acciobuild.project.enums.ProjectFeatureFlag;
import com.acciobuild.project.enums.ProjectStatus;
import com.acciobuild.project.exception.InvalidProjectOperationException;
import com.acciobuild.project.exception.ProjectArchivedException;
import com.acciobuild.project.service.ProjectFeatureFlagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service implementation managing project capability feature flags with Redis caches.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectFeatureFlagServiceImpl implements ProjectFeatureFlagService {

    private final ProjectSettingsRepository settingsRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Cacheable(value = "project-features-eval", key = "#projectId + '-' + #flag.name()")
    @Transactional(readOnly = true)
    public boolean isFeatureEnabled(UUID projectId, ProjectFeatureFlag flag) {
        log.debug("Evaluating feature flag {} for project ID: {}", flag, projectId);
        ProjectSettings settings = settingsRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project settings not found.", "SETTINGS_NOT_FOUND"));
        return getFlagValue(settings, flag);
    }

    @Override
    @CacheEvict(value = {"project-features-eval", "project-features-summary"}, allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<Void> toggleFeature(UUID projectId, ProjectFeatureFlag flag, boolean enabled) {
        log.info("Toggling feature flag {} to {} for project ID: {}", flag, enabled, projectId);

        ProjectSettings settings = settingsRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project settings not found.", "SETTINGS_NOT_FOUND"));

        if (settings.getProject().getStatus() == ProjectStatus.DELETED) {
            throw new InvalidProjectOperationException("Cannot modify feature flags of a deleted project.");
        }
        if (settings.getProject().getStatus() == ProjectStatus.ARCHIVED) {
            throw new ProjectArchivedException("Cannot modify feature flags of an archived project.");
        }

        setFlagValue(settings, flag, enabled);
        settingsRepository.save(settings);

        if (enabled) {
            eventPublisher.publishEvent(new ProjectFeatureEnabledEvent(
                    settings.getProject().getOrganizationId(), projectId, flag.name(), MdcHelper.getCorrelationId()));
        } else {
            eventPublisher.publishEvent(new ProjectFeatureDisabledEvent(
                    settings.getProject().getOrganizationId(), projectId, flag.name(), MdcHelper.getCorrelationId()));
        }

        return ApiResponse.<Void>builder()
                .status(200)
                .message("Feature flag toggled successfully.")
                .build();
    }

    @Override
    @Cacheable(value = "project-features-summary", key = "#projectId")
    @Transactional(readOnly = true)
    public ApiResponse<Map<String, Boolean>> getFeatures(UUID projectId) {
        log.info("Fetching complete feature flags summary for project ID: {}", projectId);

        ProjectSettings settings = settingsRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project settings not found.", "SETTINGS_NOT_FOUND"));

        Map<String, Boolean> features = new HashMap<>();
        for (ProjectFeatureFlag flag : ProjectFeatureFlag.values()) {
            features.put(flag.name(), getFlagValue(settings, flag));
        }

        return ApiResponse.<Map<String, Boolean>>builder()
                .status(200)
                .message("Features summary fetched.")
                .data(features)
                .build();
    }

    private boolean getFlagValue(ProjectSettings s, ProjectFeatureFlag flag) {
        return switch (flag) {
            case AI_ENABLED -> s.isAiEnabled();
            case KNOWLEDGE_CAPTURE_ENABLED -> s.isKnowledgeCaptureEnabled();
            case DOCUMENTATION_ENABLED -> s.isDocumentationEnabled();
            case CODE_ANALYSIS_ENABLED -> s.isCodeAnalysisEnabled();
            case REPOSITORY_SYNC_ENABLED -> s.isRepositorySyncEnabled();
            case WEBHOOKS_ENABLED -> s.isWebhooksEnabled();
            case CI_CD_ENABLED -> s.isCiCdEnabled();
            case API_ACCESS_ENABLED -> s.isApiAccessEnabled();
            case NOTIFICATIONS_ENABLED -> s.isNotificationsEnabled();
            case AUDIT_LOGGING_ENABLED -> s.isAuditLoggingEnabled();
        };
    }

    private void setFlagValue(ProjectSettings s, ProjectFeatureFlag flag, boolean enabled) {
        switch (flag) {
            case AI_ENABLED -> s.setAiEnabled(enabled);
            case KNOWLEDGE_CAPTURE_ENABLED -> s.setKnowledgeCaptureEnabled(enabled);
            case DOCUMENTATION_ENABLED -> s.setDocumentationEnabled(enabled);
            case CODE_ANALYSIS_ENABLED -> s.setCodeAnalysisEnabled(enabled);
            case REPOSITORY_SYNC_ENABLED -> s.setRepositorySyncEnabled(enabled);
            case WEBHOOKS_ENABLED -> s.setWebhooksEnabled(enabled);
            case CI_CD_ENABLED -> s.setCiCdEnabled(enabled);
            case API_ACCESS_ENABLED -> s.setApiAccessEnabled(enabled);
            case NOTIFICATIONS_ENABLED -> s.setNotificationsEnabled(enabled);
            case AUDIT_LOGGING_ENABLED -> s.setAuditLoggingEnabled(enabled);
        }
    }
}
