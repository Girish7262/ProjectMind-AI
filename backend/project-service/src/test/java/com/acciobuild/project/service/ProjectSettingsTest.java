package com.acciobuild.project.service;

import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.project.client.OrganizationServiceClient;
import com.acciobuild.project.domain.model.Project;
import com.acciobuild.project.domain.model.ProjectSettings;
import com.acciobuild.project.domain.repository.ProjectSettingsRepository;
import com.acciobuild.project.dto.ProjectSettingsDto;
import com.acciobuild.project.dto.SettingsDto;
import com.acciobuild.project.enums.ProjectStatus;
import com.acciobuild.project.exception.InvalidProjectOperationException;
import com.acciobuild.project.service.impl.ProjectSettingsServiceImpl;
import com.acciobuild.project.validator.ProjectSettingsValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests validating Project Settings controls, feature flag constraints,
 * and organization limit overrides.
 */
@ExtendWith(MockitoExtension.class)
public class ProjectSettingsTest {

    @Mock private ProjectSettingsRepository settingsRepository;
    @Mock private OrganizationServiceClient organizationServiceClient;
    @Mock private ProjectSettingsValidator settingsValidator;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks private ProjectSettingsServiceImpl settingsService;

    private UUID projectId;
    private Project project;
    private ProjectSettings settings;
    private ProjectSettingsDto dto;

    @BeforeEach
    void setUp() {
        projectId = UUID.randomUUID();
        project = new Project();
        project.setId(projectId);
        project.setOrganizationId(UUID.randomUUID());
        project.setStatus(ProjectStatus.ACTIVE);

        settings = new ProjectSettings();
        settings.setProject(project);
        settings.setProjectId(projectId);
        settings.setAiEnabled(true);

        dto = ProjectSettingsDto.builder()
                .projectId(projectId)
                .aiEnabled(true)
                .maxRepositories(10)
                .defaultBranch("main")
                .webhookUrl("invalid-url") // Bad format
                .build();
    }

    @Test
    void testUpdateSettings_AI_DisabledAtOrg_Failure() {
        when(settingsRepository.findById(projectId)).thenReturn(Optional.of(settings));

        SettingsDto orgSettings = new SettingsDto();
        orgSettings.setAiEnabled(false); // Organization has AI disabled

        when(organizationServiceClient.getSettings(project.getOrganizationId()))
                .thenReturn(new ApiResponse<>(200, "Success", orgSettings));

        // Stub the validator to enforce AI check
        doThrow(new InvalidProjectOperationException("AI feature is disabled at organization level."))
                .when(settingsValidator).validateSettings(any(), any());

        assertThrows(InvalidProjectOperationException.class, () -> {
            settingsService.updateSettings(projectId, dto);
        });

        verify(settingsRepository, never()).save(any());
    }

    @Test
    void testUpdateSettings_WebhookValidation_Failure() {
        ProjectSettingsValidator validator = new ProjectSettingsValidator();
        SettingsDto orgSettings = new SettingsDto();
        orgSettings.setAiEnabled(true);
        orgSettings.setMaxProjects(10);

        assertThrows(InvalidProjectOperationException.class, () -> {
            validator.validateSettings(dto, orgSettings);
        });
    }

    @Test
    void testUpdateSettings_WebhookValidation_Success() {
        ProjectSettingsValidator validator = new ProjectSettingsValidator();
        SettingsDto orgSettings = new SettingsDto();
        orgSettings.setAiEnabled(true);
        orgSettings.setMaxProjects(10);

        dto.setWebhookUrl("https://hooks.acciobuild.com/v1");

        assertDoesNotThrow(() -> {
            validator.validateSettings(dto, orgSettings);
        });
    }
}
