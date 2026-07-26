package com.acciobuild.organization.service;

import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.common.exception.BusinessException;
import com.acciobuild.organization.domain.model.OrganizationSettings;
import com.acciobuild.organization.domain.repository.OrganizationSettingsRepository;
import com.acciobuild.organization.dto.SettingsDto;
import com.acciobuild.organization.enums.FeatureFlag;
import com.acciobuild.organization.service.impl.FeatureFlagServiceImpl;
import com.acciobuild.organization.service.impl.SettingsServiceImpl;
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
 * Unit tests validating Feature Flags, operational configuration limits, and validations.
 */
@ExtendWith(MockitoExtension.class)
public class FeatureFlagServiceTest {

    @Mock private OrganizationSettingsRepository settingsRepository;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks private FeatureFlagServiceImpl featureFlagService;
    @InjectMocks private SettingsServiceImpl settingsService;

    private UUID organizationId;
    private OrganizationSettings settings;

    @BeforeEach
    void setUp() {
        organizationId = UUID.randomUUID();
        settings = new OrganizationSettings();
        settings.setOrganizationId(organizationId);
        settings.setAiEnabled(true);
        settings.setKnowledgeBaseEnabled(true);
        settings.setDefaultTimezone("UTC");
        settings.setDefaultLanguage("en");
    }

    @Test
    void testIsFeatureEnabled_Success() {
        when(settingsRepository.findById(organizationId)).thenReturn(Optional.of(settings));

        boolean enabled = featureFlagService.isFeatureEnabled(organizationId, FeatureFlag.AI_ENABLED);

        assertTrue(enabled);
        verify(settingsRepository).findById(organizationId);
    }

    @Test
    void testToggleFeature_Disable_Success() {
        when(settingsRepository.findById(organizationId)).thenReturn(Optional.of(settings));
        when(settingsRepository.save(any(OrganizationSettings.class))).thenReturn(settings);

        ApiResponse<SettingsDto> response = featureFlagService.disableFeature(organizationId, FeatureFlag.AI_ENABLED);

        assertNotNull(response);
        assertFalse(settings.isAiEnabled());
        verify(eventPublisher).publishEvent(any(Object.class));
    }

    @Test
    void testUpdateSettings_InvalidTimezone_Failure() {
        when(settingsRepository.findById(organizationId)).thenReturn(Optional.of(settings));

        SettingsDto invalidDto = SettingsDto.builder()
                .defaultTimezone("Invalid/Timezone_GMT") // Not a valid ZoneId
                .defaultLanguage("en")
                .maxMembers(10)
                .maxProjects(5)
                .build();

        assertThrows(BusinessException.class, () -> {
            settingsService.updateSettings(organizationId, invalidDto);
        });

        verify(settingsRepository, never()).save(any());
    }

    @Test
    void testUpdateSettings_InvalidLanguage_Failure() {
        when(settingsRepository.findById(organizationId)).thenReturn(Optional.of(settings));

        SettingsDto invalidDto = SettingsDto.builder()
                .defaultTimezone("UTC")
                .defaultLanguage("english") // Expects 2-letter ISO code
                .maxMembers(10)
                .maxProjects(5)
                .build();

        assertThrows(BusinessException.class, () -> {
            settingsService.updateSettings(organizationId, invalidDto);
        });

        verify(settingsRepository, never()).save(any());
    }
}
