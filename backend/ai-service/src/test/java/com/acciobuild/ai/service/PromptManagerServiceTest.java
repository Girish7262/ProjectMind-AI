package com.acciobuild.ai.service;

import com.acciobuild.ai.domain.model.AiPromptTemplate;
import com.acciobuild.ai.domain.model.AiPromptVersion;
import com.acciobuild.ai.domain.repository.AiPromptTemplateRepository;
import com.acciobuild.ai.domain.repository.AiPromptVersionRepository;
import com.acciobuild.ai.dto.PromptTemplateDto;
import com.acciobuild.ai.dto.PromptVersionDto;
import com.acciobuild.ai.enums.PromptStatus;
import com.acciobuild.ai.multitenancy.TenantContext;
import com.acciobuild.ai.prompt.PromptValidator;
import com.acciobuild.ai.prompt.PromptVersionManager;
import com.acciobuild.ai.service.impl.PromptManagerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PromptManagerService verifying lifecycle and tenant boundary logic.
 */
@ExtendWith(MockitoExtension.class)
public class PromptManagerServiceTest {

    @Mock
    private AiPromptTemplateRepository templateRepository;
    @Mock
    private AiPromptVersionRepository versionRepository;
    @Mock
    private PromptVersionManager versionManager;
    @Mock
    private PromptValidator promptValidator;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private PromptManagerServiceImpl promptManagerService;

    private UUID tenantId;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        TenantContext.setCurrentTenant(tenantId);
    }

    @Test
    void testCreatePromptSuccessfully() {
        PromptTemplateDto dto = PromptTemplateDto.builder()
                .name("Customer Support")
                .description("Support desk template")
                .build();

        when(templateRepository.findByName("Customer Support")).thenReturn(Optional.empty());

        AiPromptTemplate template = new AiPromptTemplate();
        template.setId(UUID.randomUUID());
        template.setOrganizationId(tenantId);
        template.setName(dto.getName());
        template.setDescription(dto.getDescription());
        template.setStatus(PromptStatus.DRAFT);

        when(templateRepository.save(any(AiPromptTemplate.class))).thenReturn(template);

        PromptTemplateDto result = promptManagerService.createPrompt(dto);

        assertNotNull(result);
        assertEquals("Customer Support", result.getName());
        verify(eventPublisher, times(1)).publishEvent(any(Object.class));
    }

    @Test
    void testCreatePromptTenantValidationFailure() {
        PromptTemplateDto dto = PromptTemplateDto.builder()
                .name("Finance Agent")
                .build();

        doThrow(new SecurityException("Access Denied"))
                .when(promptValidator).validateTemplate(any());

        assertThrows(SecurityException.class, () -> {
            promptManagerService.createPrompt(dto);
        });
    }

    @Test
    void testClonePromptDuplicatesTemplateAndVersions() {
        UUID promptId = UUID.randomUUID();
        AiPromptTemplate source = new AiPromptTemplate();
        source.setId(promptId);
        source.setOrganizationId(tenantId);
        source.setName("Source Prompt");

        when(templateRepository.findById(promptId)).thenReturn(Optional.of(source));
        when(templateRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AiPromptVersion v1 = new AiPromptVersion();
        v1.setTemplate(source);
        v1.setVersionNumber(1);
        v1.setSystemInstruction("Help users.");
        v1.setIsActive(true);

        when(versionRepository.findByTemplateId(promptId)).thenReturn(List.of(v1));

        PromptTemplateDto cloned = promptManagerService.clonePrompt(promptId);

        assertNotNull(cloned);
        assertTrue(cloned.getName().contains("Source Prompt - Copy"));
        verify(versionRepository, times(1)).save(any());
    }

    @Test
    void testActivateVersionSuccessfully() {
        UUID promptId = UUID.randomUUID();
        AiPromptTemplate template = new AiPromptTemplate();
        template.setId(promptId);
        template.setOrganizationId(tenantId);

        AiPromptVersion version = new AiPromptVersion();
        version.setTemplate(template);
        version.setVersionNumber(2);

        when(versionManager.activateVersion(promptId, 2)).thenReturn(version);
        when(templateRepository.save(any())).thenReturn(template);

        PromptTemplateDto activated = promptManagerService.activateVersion(promptId, 2);

        assertNotNull(activated);
        verify(eventPublisher, times(1)).publishEvent(any(Object.class));
    }
}
