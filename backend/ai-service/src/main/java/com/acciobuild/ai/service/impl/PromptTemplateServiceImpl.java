package com.acciobuild.ai.service.impl;

import com.acciobuild.ai.domain.event.PromptTemplateActivatedEvent;
import com.acciobuild.ai.domain.event.PromptTemplateCreatedEvent;
import com.acciobuild.ai.domain.model.AiPromptTemplate;
import com.acciobuild.ai.domain.model.AiPromptVersion;
import com.acciobuild.ai.domain.repository.AiPromptTemplateRepository;
import com.acciobuild.ai.domain.repository.AiPromptVersionRepository;
import com.acciobuild.ai.dto.PromptTemplateDto;
import com.acciobuild.ai.dto.PromptVersionDto;
import com.acciobuild.ai.enums.PromptStatus;
import com.acciobuild.ai.exception.DuplicatePromptTemplateException;
import com.acciobuild.ai.exception.InvalidConversationStateException;
import com.acciobuild.ai.service.PromptTemplateService;
import com.acciobuild.ai.multitenancy.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service Implementation managing AI System and User Prompt templates.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PromptTemplateServiceImpl implements PromptTemplateService {

    private final AiPromptTemplateRepository templateRepository;
    private final AiPromptVersionRepository versionRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public PromptTemplateDto createTemplate(PromptTemplateDto dto) {
        log.info("Creating prompt template: {}", dto.getName());

        if (templateRepository.findByName(dto.getName()).isPresent()) {
            throw new DuplicatePromptTemplateException("Prompt template name already exists: " + dto.getName());
        }

        AiPromptTemplate template = new AiPromptTemplate();
        template.setId(UUID.randomUUID());
        template.setOrganizationId(TenantContext.getCurrentTenant() != null ? TenantContext.getCurrentTenant() : UUID.randomUUID());
        template.setName(dto.getName());
        template.setDescription(dto.getDescription());
        template.setStatus(PromptStatus.DRAFT);
        template.setCreatedAt(LocalDateTime.now());
        template.setUpdatedAt(LocalDateTime.now());

        AiPromptTemplate saved = templateRepository.save(template);

        eventPublisher.publishEvent(new PromptTemplateCreatedEvent(
                saved.getOrganizationId(),
                saved.getId(),
                saved.getName(),
                UUID.randomUUID().toString()
        ));

        return mapToDto(saved);
    }

    @Override
    @Transactional
    public PromptVersionDto addVersion(UUID templateId, PromptVersionDto versionDto) {
        log.info("Creating immutable version for template: {}", templateId);

        AiPromptTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new InvalidConversationStateException("Template not found: " + templateId));

        List<AiPromptVersion> versions = versionRepository.findByTemplateId(templateId);
        int nextVersion = versions.stream()
                .mapToInt(AiPromptVersion::getVersionNumber)
                .max()
                .orElse(0) + 1;

        AiPromptVersion version = new AiPromptVersion();
        version.setId(UUID.randomUUID());
        version.setOrganizationId(template.getOrganizationId());
        version.setTemplate(template);
        version.setVersionNumber(nextVersion);
        version.setSystemInstruction(versionDto.getSystemInstruction());
        version.setUserTemplate(versionDto.getUserTemplate());
        version.setParametersJson(versionDto.getParametersJson());
        version.setIsActive(false);
        version.setCreatedBy(versionDto.getCreatedBy() != null ? versionDto.getCreatedBy() : UUID.randomUUID());
        version.setCreatedAt(LocalDateTime.now());

        AiPromptVersion saved = versionRepository.save(version);
        return mapToVersionDto(saved);
    }

    @Override
    @Transactional
    public PromptTemplateDto activateVersion(UUID templateId, Integer versionNumber) {
        log.info("Activating version {} for template {}", versionNumber, templateId);

        AiPromptTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new InvalidConversationStateException("Template not found: " + templateId));

        List<AiPromptVersion> versions = versionRepository.findByTemplateId(templateId);
        AiPromptVersion target = versions.stream()
                .filter(v -> v.getVersionNumber().equals(versionNumber))
                .findFirst()
                .orElseThrow(() -> new InvalidConversationStateException("Version number " + versionNumber + " not found."));

        for (AiPromptVersion v : versions) {
            v.setIsActive(v.getId().equals(target.getId()));
            versionRepository.save(v);
        }

        template.setStatus(PromptStatus.ACTIVE);
        template.setUpdatedAt(LocalDateTime.now());
        AiPromptTemplate saved = templateRepository.save(template);

        eventPublisher.publishEvent(new PromptTemplateActivatedEvent(
                template.getOrganizationId(),
                template.getId(),
                versionNumber,
                UUID.randomUUID().toString()
        ));

        return mapToDto(saved);
    }

    @Override
    public PromptTemplateDto getTemplateByName(String name) {
        AiPromptTemplate template = templateRepository.findByName(name)
                .orElseThrow(() -> new InvalidConversationStateException("Template not found with name: " + name));
        return mapToDto(template);
    }

    private PromptTemplateDto mapToDto(AiPromptTemplate t) {
        return PromptTemplateDto.builder()
                .id(t.getId())
                .organizationId(t.getOrganizationId())
                .name(t.getName())
                .description(t.getDescription())
                .status(t.getStatus())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }

    private PromptVersionDto mapToVersionDto(AiPromptVersion v) {
        return PromptVersionDto.builder()
                .id(v.getId())
                .versionNumber(v.getVersionNumber())
                .systemInstruction(v.getSystemInstruction())
                .userTemplate(v.getUserTemplate())
                .parametersJson(v.getParametersJson())
                .isActive(v.getIsActive())
                .createdBy(v.getCreatedBy())
                .createdAt(v.getCreatedAt())
                .build();
    }
}
