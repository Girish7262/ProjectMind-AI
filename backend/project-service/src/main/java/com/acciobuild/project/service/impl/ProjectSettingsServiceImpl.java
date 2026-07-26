package com.acciobuild.project.service.impl;

import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.common.exception.ResourceNotFoundException;
import com.acciobuild.common.util.MdcHelper;
import com.acciobuild.project.client.OrganizationServiceClient;
import com.acciobuild.project.domain.event.ProjectSettingsUpdatedEvent;
import com.acciobuild.project.domain.model.ProjectSettings;
import com.acciobuild.project.domain.repository.ProjectSettingsRepository;
import com.acciobuild.project.dto.ProjectSettingsDto;
import com.acciobuild.project.dto.SettingsDto;
import com.acciobuild.project.enums.ProjectStatus;
import com.acciobuild.project.exception.InvalidProjectOperationException;
import com.acciobuild.project.exception.ProjectArchivedException;
import com.acciobuild.project.service.ProjectSettingsService;
import com.acciobuild.project.validator.ProjectSettingsValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

/**
 * Service implementation managing project settings thresholds, checking limits against
 * parent organization constraints and caching lookups using Redis.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectSettingsServiceImpl implements ProjectSettingsService {

    private final ProjectSettingsRepository settingsRepository;
    private final OrganizationServiceClient organizationServiceClient;
    private final ProjectSettingsValidator settingsValidator;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Cacheable(value = "project-settings", key = "#projectId")
    @Transactional(readOnly = true)
    public ApiResponse<ProjectSettingsDto> getSettings(UUID projectId) {
        log.info("Fetching project settings for project ID: {}", projectId);

        ProjectSettings settings = settingsRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project settings not found.", "SETTINGS_NOT_FOUND"));

        return ApiResponse.<ProjectSettingsDto>builder()
                .status(200)
                .message("Project settings fetched successfully.")
                .data(mapToDto(settings))
                .build();
    }

    @Override
    @CacheEvict(value = "project-settings", key = "#projectId")
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<ProjectSettingsDto> updateSettings(UUID projectId, ProjectSettingsDto dto) {
        log.info("Updating settings for project ID: {}", projectId);

        ProjectSettings settings = settingsRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project settings not found.", "SETTINGS_NOT_FOUND"));

        if (settings.getProject().getStatus() == ProjectStatus.DELETED) {
            throw new InvalidProjectOperationException("Cannot modify settings of a deleted project.");
        }
        if (settings.getProject().getStatus() == ProjectStatus.ARCHIVED) {
            throw new ProjectArchivedException("Cannot modify settings of an archived project.");
        }

        // Validate limits against organization parameters
        try {
            ApiResponse<SettingsDto> orgRes = organizationServiceClient.getSettings(settings.getProject().getOrganizationId());
            if (orgRes != null && orgRes.getData() != null) {
                settingsValidator.validateSettings(dto, orgRes.getData());
            }
        } catch (InvalidProjectOperationException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Could not retrieve organization configurations via Feign. Proceeding with internal validation rules.", e);
        }

        settings.setAiEnabled(dto.isAiEnabled());
        settings.setKnowledgeCaptureEnabled(dto.isKnowledgeCaptureEnabled());
        settings.setCodeAnalysisEnabled(dto.isCodeAnalysisEnabled());
        settings.setDocumentationEnabled(dto.isDocumentationEnabled());
        settings.setMaxRepositories(dto.getMaxRepositories());
        settings.setDefaultBranch(dto.getDefaultBranch());
        settings.setRepositorySyncEnabled(dto.isRepositorySyncEnabled());
        settings.setWebhooksEnabled(dto.isWebhooksEnabled());
        settings.setCiCdEnabled(dto.isCiCdEnabled());
        settings.setApiAccessEnabled(dto.isApiAccessEnabled());
        settings.setNotificationsEnabled(dto.isNotificationsEnabled());
        settings.setAuditLoggingEnabled(dto.isAuditLoggingEnabled());
        settings.setMaxDocuments(dto.getMaxDocuments());
        settings.setMaxTeamMembers(dto.getMaxTeamMembers());
        settings.setStorageLimitGb(dto.getStorageLimitGb());
        settings.setDailyAiRequests(dto.getDailyAiRequests());
        settings.setWebhookUrl(dto.getWebhookUrl());
        settings.setAllowedRepositoryProviders(dto.getAllowedRepositoryProviders());
        settings.setCodeAnalysisProfile(dto.getCodeAnalysisProfile());
        settings.setDocumentationTemplate(dto.getDocumentationTemplate());

        ProjectSettings saved = settingsRepository.save(settings);

        eventPublisher.publishEvent(new ProjectSettingsUpdatedEvent(
                settings.getProject().getOrganizationId(), projectId, MdcHelper.getCorrelationId()));

        return ApiResponse.<ProjectSettingsDto>builder()
                .status(200)
                .message("Project settings updated successfully.")
                .data(mapToDto(saved))
                .build();
    }

    private ProjectSettingsDto mapToDto(ProjectSettings s) {
        if (s == null) return null;
        return ProjectSettingsDto.builder()
                .projectId(s.getProjectId())
                .aiEnabled(s.isAiEnabled())
                .knowledgeCaptureEnabled(s.isKnowledgeCaptureEnabled())
                .codeAnalysisEnabled(s.isCodeAnalysisEnabled())
                .documentationEnabled(s.isDocumentationEnabled())
                .maxRepositories(s.getMaxRepositories())
                .defaultBranch(s.getDefaultBranch())
                .repositorySyncEnabled(s.isRepositorySyncEnabled())
                .webhooksEnabled(s.isWebhooksEnabled())
                .ciCdEnabled(s.isCiCdEnabled())
                .apiAccessEnabled(s.isApiAccessEnabled())
                .notificationsEnabled(s.isNotificationsEnabled())
                .auditLoggingEnabled(s.isAuditLoggingEnabled())
                .maxDocuments(s.getMaxDocuments())
                .maxTeamMembers(s.getMaxTeamMembers())
                .storageLimitGb(s.getStorageLimitGb())
                .dailyAiRequests(s.getDailyAiRequests())
                .webhookUrl(s.getWebhookUrl())
                .allowedRepositoryProviders(s.getAllowedRepositoryProviders())
                .codeAnalysisProfile(s.getCodeAnalysisProfile())
                .documentationTemplate(s.getDocumentationTemplate())
                .build();
    }
}
