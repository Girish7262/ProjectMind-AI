package com.acciobuild.organization.service.impl;

import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.common.exception.ResourceNotFoundException;
import com.acciobuild.common.util.MdcHelper;
import com.acciobuild.organization.domain.event.FeatureDisabledEvent;
import com.acciobuild.organization.domain.event.FeatureEnabledEvent;
import com.acciobuild.organization.domain.model.OrganizationSettings;
import com.acciobuild.organization.domain.repository.OrganizationSettingsRepository;
import com.acciobuild.organization.dto.SettingsDto;
import com.acciobuild.organization.enums.FeatureFlag;
import com.acciobuild.organization.service.FeatureFlagService;
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
 * Service implementation managing organization Feature Flags, event triggers,
 * and Redis Cache synchronizations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FeatureFlagServiceImpl implements FeatureFlagService {

    private final OrganizationSettingsRepository settingsRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "features", key = "#organizationId")
    public boolean isFeatureEnabled(UUID organizationId, FeatureFlag feature) {
        log.info("Checking feature flag: {} for organization: {}", feature, organizationId);
        OrganizationSettings settings = settingsRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Settings not found.", "SETTINGS_NOT_FOUND"));
        return getFeatureValue(settings, feature);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"features", "org_settings"}, key = "#organizationId")
    public ApiResponse<SettingsDto> enableFeature(UUID organizationId, FeatureFlag feature) {
        log.info("Enabling feature flag: {} for organization: {}", feature, organizationId);
        OrganizationSettings settings = settingsRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Settings not found.", "SETTINGS_NOT_FOUND"));

        setFeatureValue(settings, feature, true);
        OrganizationSettings saved = settingsRepository.save(settings);

        eventPublisher.publishEvent(new FeatureEnabledEvent(organizationId, feature.name(), MdcHelper.getCorrelationId()));

        return ApiResponse.<SettingsDto>builder()
                .status(200)
                .message("Feature flag enabled successfully.")
                .data(mapToDto(saved))
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"features", "org_settings"}, key = "#organizationId")
    public ApiResponse<SettingsDto> disableFeature(UUID organizationId, FeatureFlag feature) {
        log.info("Disabling feature flag: {} for organization: {}", feature, organizationId);
        OrganizationSettings settings = settingsRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Settings not found.", "SETTINGS_NOT_FOUND"));

        setFeatureValue(settings, feature, false);
        OrganizationSettings saved = settingsRepository.save(settings);

        eventPublisher.publishEvent(new FeatureDisabledEvent(organizationId, feature.name(), MdcHelper.getCorrelationId()));

        return ApiResponse.<SettingsDto>builder()
                .status(200)
                .message("Feature flag disabled successfully.")
                .data(mapToDto(saved))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "org_settings", key = "#organizationId")
    public ApiResponse<Map<String, Boolean>> getFeatures(UUID organizationId) {
        OrganizationSettings settings = settingsRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Settings not found.", "SETTINGS_NOT_FOUND"));

        Map<String, Boolean> features = new HashMap<>();
        for (FeatureFlag f : FeatureFlag.values()) {
            features.put(f.name(), getFeatureValue(settings, f));
        }

        return ApiResponse.<Map<String, Boolean>>builder()
                .status(200)
                .message("Features status fetched.")
                .data(features)
                .build();
    }

    private boolean getFeatureValue(OrganizationSettings s, FeatureFlag feature) {
        return switch (feature) {
            case AI_ENABLED -> s.isAiEnabled();
            case KNOWLEDGE_BASE_ENABLED -> s.isKnowledgeBaseEnabled();
            case PROJECT_MODULE_ENABLED -> s.isProjectModuleEnabled();
            case DOCUMENT_UPLOAD_ENABLED -> s.isDocumentUploadEnabled();
            case API_ACCESS_ENABLED -> s.isApiAccessEnabled();
            case TEAM_MANAGEMENT_ENABLED -> s.isTeamManagementEnabled();
            case AUDIT_LOGS_ENABLED -> s.isAuditLogsEnabled();
            case NOTIFICATIONS_ENABLED -> s.isNotificationsEnabled();
        };
    }

    private void setFeatureValue(OrganizationSettings s, FeatureFlag feature, boolean val) {
        switch (feature) {
            case AI_ENABLED -> s.setAiEnabled(val);
            case KNOWLEDGE_BASE_ENABLED -> s.setKnowledgeBaseEnabled(val);
            case PROJECT_MODULE_ENABLED -> s.setProjectModuleEnabled(val);
            case DOCUMENT_UPLOAD_ENABLED -> s.setDocumentUploadEnabled(val);
            case API_ACCESS_ENABLED -> s.setApiAccessEnabled(val);
            case TEAM_MANAGEMENT_ENABLED -> s.setTeamManagementEnabled(val);
            case AUDIT_LOGS_ENABLED -> s.setAuditLogsEnabled(val);
            case NOTIFICATIONS_ENABLED -> s.setNotificationsEnabled(val);
        }
    }

    private SettingsDto mapToDto(OrganizationSettings s) {
        if (s == null) return null;
        return SettingsDto.builder()
                .organizationId(s.getOrganizationId())
                .aiEnabled(s.isAiEnabled())
                .knowledgeSharingEnabled(s.isKnowledgeSharingEnabled())
                .defaultVisibility(s.getDefaultVisibility())
                .maxProjects(s.getMaxProjects())
                .maxMembers(s.getMaxMembers())
                .knowledgeBaseEnabled(s.isKnowledgeBaseEnabled())
                .projectModuleEnabled(s.isProjectModuleEnabled())
                .documentUploadEnabled(s.isDocumentUploadEnabled())
                .apiAccessEnabled(s.isApiAccessEnabled())
                .teamManagementEnabled(s.isTeamManagementEnabled())
                .auditLogsEnabled(s.isAuditLogsEnabled())
                .notificationsEnabled(s.isNotificationsEnabled())
                .maxStorageGb(s.getMaxStorageGb())
                .maxApiRequestsPerDay(s.getMaxApiRequestsPerDay())
                .allowedFileSize(s.getAllowedFileSize())
                .allowedFileTypes(s.getAllowedFileTypes())
                .defaultLanguage(s.getDefaultLanguage())
                .defaultTimezone(s.getDefaultTimezone())
                .build();
    }
}
