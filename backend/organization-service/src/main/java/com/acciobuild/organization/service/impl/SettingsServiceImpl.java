package com.acciobuild.organization.service.impl;

import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.common.exception.BusinessException;
import com.acciobuild.common.exception.ResourceNotFoundException;
import com.acciobuild.common.util.MdcHelper;
import com.acciobuild.organization.domain.event.OrganizationSettingsUpdatedEvent;
import com.acciobuild.organization.domain.model.OrganizationSettings;
import com.acciobuild.organization.domain.repository.OrganizationSettingsRepository;
import com.acciobuild.organization.dto.SettingsDto;
import com.acciobuild.organization.service.SettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.ZoneId;
import java.util.UUID;

/**
 * Service implementation managing tenant configuration settings, validation constraints,
 * and Redis Cache.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SettingsServiceImpl implements SettingsService {

    private final OrganizationSettingsRepository settingsRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "org_settings", key = "#organizationId")
    public ApiResponse<SettingsDto> getSettings(UUID organizationId) {
        log.info("Fetching settings parameters for organization ID: {}", organizationId);

        OrganizationSettings settings = settingsRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization settings not found.", "SETTINGS_NOT_FOUND"));

        return ApiResponse.<SettingsDto>builder()
                .status(200)
                .message("Settings retrieved successfully.")
                .data(mapToDto(settings))
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"org_settings", "features"}, key = "#organizationId")
    public ApiResponse<SettingsDto> updateSettings(UUID organizationId, SettingsDto dto) {
        log.info("Updating settings limits parameters for organization ID: {}", organizationId);

        OrganizationSettings settings = settingsRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization settings not found.", "SETTINGS_NOT_FOUND"));

        // Validations
        if (dto.getMaxMembers() < 0 || dto.getMaxProjects() < 0) {
            throw new BusinessException("Maximum limits parameters cannot be negative.", "INVALID_LIMITS");
        }

        if (dto.getDefaultTimezone() != null) {
            try {
                ZoneId.of(dto.getDefaultTimezone());
            } catch (Exception e) {
                throw new BusinessException("Invalid timezone identifier specified.", "INVALID_TIMEZONE");
            }
        }

        if (dto.getDefaultLanguage() != null && dto.getDefaultLanguage().length() != 2) {
            throw new BusinessException("Language must be a supported 2-letter ISO code.", "INVALID_LANGUAGE");
        }

        settings.setAiEnabled(dto.isAiEnabled());
        settings.setKnowledgeSharingEnabled(dto.isKnowledgeSharingEnabled());
        settings.setDefaultVisibility(dto.getDefaultVisibility());
        settings.setMaxProjects(dto.getMaxProjects());
        settings.setMaxMembers(dto.getMaxMembers());
        
        // Enhance mappings
        settings.setKnowledgeBaseEnabled(dto.isKnowledgeBaseEnabled());
        settings.setProjectModuleEnabled(dto.isProjectModuleEnabled());
        settings.setDocumentUploadEnabled(dto.isDocumentUploadEnabled());
        settings.setApiAccessEnabled(dto.isApiAccessEnabled());
        settings.setTeamManagementEnabled(dto.isTeamManagementEnabled());
        settings.setAuditLogsEnabled(dto.isAuditLogsEnabled());
        settings.setNotificationsEnabled(dto.isNotificationsEnabled());
        settings.setMaxStorageGb(dto.getMaxStorageGb());
        settings.setMaxApiRequestsPerDay(dto.getMaxApiRequestsPerDay());
        settings.setAllowedFileSize(dto.getAllowedFileSize());
        settings.setAllowedFileTypes(dto.getAllowedFileTypes());
        settings.setDefaultLanguage(dto.getDefaultLanguage());
        settings.setDefaultTimezone(dto.getDefaultTimezone());

        OrganizationSettings saved = settingsRepository.save(settings);

        eventPublisher.publishEvent(new OrganizationSettingsUpdatedEvent(organizationId, MdcHelper.getCorrelationId()));

        return ApiResponse.<SettingsDto>builder()
                .status(200)
                .message("Settings parameters updated successfully.")
                .data(mapToDto(saved))
                .build();
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
