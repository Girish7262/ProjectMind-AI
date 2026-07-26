package com.acciobuild.ai.service.impl;

import com.acciobuild.ai.domain.event.PromptCreatedEvent;
import com.acciobuild.ai.domain.event.PromptUpdatedEvent;
import com.acciobuild.ai.domain.event.PromptActivatedEvent;
import com.acciobuild.ai.domain.event.PromptVersionCreatedEvent;
import com.acciobuild.ai.domain.model.AiPromptTemplate;
import com.acciobuild.ai.domain.model.AiPromptVersion;
import com.acciobuild.ai.domain.repository.AiPromptTemplateRepository;
import com.acciobuild.ai.domain.repository.AiPromptVersionRepository;
import com.acciobuild.ai.dto.PromptTemplateDto;
import com.acciobuild.ai.dto.PromptVersionDto;
import com.acciobuild.ai.enums.PromptStatus;
import com.acciobuild.ai.exception.DuplicatePromptTemplateException;
import com.acciobuild.ai.exception.InvalidConversationStateException;
import com.acciobuild.ai.multitenancy.TenantContext;
import com.acciobuild.ai.prompt.PromptValidator;
import com.acciobuild.ai.prompt.PromptVersionManager;
import com.acciobuild.ai.service.PromptManagerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service implementation managing Prompt Templates and Snapshot revisions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PromptManagerServiceImpl implements PromptManagerService {

    private final AiPromptTemplateRepository templateRepository;
    private final AiPromptVersionRepository versionRepository;
    private final PromptVersionManager versionManager;
    private final PromptValidator promptValidator;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    @CacheEvict(value = "promptTemplates", allEntries = true)
    public PromptTemplateDto createPrompt(PromptTemplateDto dto) {
        log.info("Creating new prompt template: {}", dto.getName());

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

        promptValidator.validateTemplate(template);

        AiPromptTemplate saved = templateRepository.save(template);

        eventPublisher.publishEvent(new PromptCreatedEvent(
                saved.getOrganizationId(),
                saved.getId(),
                saved.getName(),
                UUID.randomUUID().toString()
        ));

        return mapToDto(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = "promptTemplates", key = "#promptId")
    public PromptTemplateDto updatePrompt(UUID promptId, PromptTemplateDto dto) {
        log.info("Updating prompt template: {}", promptId);

        AiPromptTemplate template = templateRepository.findById(promptId)
                .orElseThrow(() -> new InvalidConversationStateException("Prompt template not found: " + promptId));

        validateTenant(template.getOrganizationId());

        template.setDescription(dto.getDescription());
        template.setUpdatedAt(LocalDateTime.now());
        
        if (dto.getName() != null && !dto.getName().equals(template.getName())) {
            if (templateRepository.findByName(dto.getName()).isPresent()) {
                throw new DuplicatePromptTemplateException("Prompt template name already exists: " + dto.getName());
            }
            template.setName(dto.getName());
        }

        promptValidator.validateTemplate(template);
        AiPromptTemplate saved = templateRepository.save(template);

        eventPublisher.publishEvent(new PromptUpdatedEvent(
                saved.getOrganizationId(),
                saved.getId(),
                saved.getName(),
                UUID.randomUUID().toString()
        ));

        return mapToDto(saved);
    }

    @Override
    @Cacheable(value = "promptTemplates", key = "#promptId")
    public PromptTemplateDto getPrompt(UUID promptId) {
        log.info("Retrieving prompt template: {}", promptId);
        AiPromptTemplate template = templateRepository.findById(promptId)
                .orElseThrow(() -> new InvalidConversationStateException("Prompt template not found: " + promptId));

        validateTenant(template.getOrganizationId());
        return mapToDto(template);
    }

    @Override
    @Transactional
    @CacheEvict(value = "promptTemplates", allEntries = true)
    public PromptTemplateDto clonePrompt(UUID promptId) {
        log.info("Cloning prompt template: {}", promptId);
        AiPromptTemplate source = templateRepository.findById(promptId)
                .orElseThrow(() -> new InvalidConversationStateException("Prompt template not found to clone: " + promptId));

        validateTenant(source.getOrganizationId());

        AiPromptTemplate target = new AiPromptTemplate();
        target.setId(UUID.randomUUID());
        target.setOrganizationId(source.getOrganizationId());
        String randomSuffix = UUID.randomUUID().toString().substring(0, 4);
        target.setName(source.getName() + " - Copy (" + randomSuffix + ")");
        target.setDescription(source.getDescription());
        target.setStatus(PromptStatus.DRAFT);
        target.setCreatedAt(LocalDateTime.now());
        target.setUpdatedAt(LocalDateTime.now());

        promptValidator.validateTemplate(target);
        AiPromptTemplate saved = templateRepository.save(target);

        List<AiPromptVersion> versions = versionRepository.findByTemplateId(promptId);
        for (AiPromptVersion sourceVersion : versions) {
            AiPromptVersion targetVersion = new AiPromptVersion();
            targetVersion.setId(UUID.randomUUID());
            targetVersion.setOrganizationId(saved.getOrganizationId());
            targetVersion.setTemplate(saved);
            targetVersion.setVersionNumber(sourceVersion.getVersionNumber());
            targetVersion.setSystemInstruction(sourceVersion.getSystemInstruction());
            targetVersion.setUserTemplate(sourceVersion.getUserTemplate());
            targetVersion.setParametersJson(sourceVersion.getParametersJson());
            targetVersion.setIsActive(sourceVersion.getIsActive());
            targetVersion.setCreatedBy(sourceVersion.getCreatedBy());
            targetVersion.setCreatedAt(LocalDateTime.now());
            versionRepository.save(targetVersion);
        }

        eventPublisher.publishEvent(new PromptCreatedEvent(
                saved.getOrganizationId(),
                saved.getId(),
                saved.getName(),
                UUID.randomUUID().toString()
        ));

        return mapToDto(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = "promptTemplates", key = "#promptId")
    public void archivePrompt(UUID promptId) {
        log.info("Archiving prompt template: {}", promptId);
        AiPromptTemplate template = templateRepository.findById(promptId)
                .orElseThrow(() -> new InvalidConversationStateException("Prompt template not found to archive: " + promptId));

        validateTenant(template.getOrganizationId());
        template.setStatus(PromptStatus.DEPRECATED);
        template.setUpdatedAt(LocalDateTime.now());
        templateRepository.save(template);
    }

    @Override
    @Transactional
    @CacheEvict(value = "promptTemplates", key = "#promptId")
    public void restorePrompt(UUID promptId) {
        log.info("Restoring prompt template: {}", promptId);
        AiPromptTemplate template = templateRepository.findById(promptId)
                .orElseThrow(() -> new InvalidConversationStateException("Prompt template not found to restore: " + promptId));

        validateTenant(template.getOrganizationId());
        template.setStatus(PromptStatus.ACTIVE);
        template.setUpdatedAt(LocalDateTime.now());
        templateRepository.save(template);
    }

    @Override
    @Transactional
    @CacheEvict(value = "promptTemplates", key = "#promptId")
    public void deletePrompt(UUID promptId) {
        log.info("Deleting prompt template: {}", promptId);
        AiPromptTemplate template = templateRepository.findById(promptId)
                .orElseThrow(() -> new InvalidConversationStateException("Prompt template not found to delete: " + promptId));

        validateTenant(template.getOrganizationId());
        templateRepository.delete(template);
    }

    @Override
    @Transactional
    @CacheEvict(value = "activePromptVersions", key = "#promptId")
    public PromptVersionDto createVersion(UUID promptId, PromptVersionDto versionDto) {
        PromptVersionDto version = versionManager.createVersion(promptId, versionDto);
        
        eventPublisher.publishEvent(new PromptVersionCreatedEvent(
                version.getOrganizationId(),
                promptId,
                version.getId(),
                version.getVersionNumber(),
                UUID.randomUUID().toString()
        ));
        
        return version;
    }

    @Override
    @Transactional
    @CacheEvict(value = "activePromptVersions", key = "#promptId")
    public PromptTemplateDto activateVersion(UUID promptId, Integer versionNumber) {
        AiPromptVersion active = versionManager.activateVersion(promptId, versionNumber);
        
        AiPromptTemplate template = active.getTemplate();
        template.setStatus(PromptStatus.ACTIVE);
        template.setUpdatedAt(LocalDateTime.now());
        AiPromptTemplate saved = templateRepository.save(template);

        eventPublisher.publishEvent(new PromptActivatedEvent(
                saved.getOrganizationId(),
                promptId,
                versionNumber,
                UUID.randomUUID().toString()
        ));

        return mapToDto(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = "activePromptVersions", key = "#promptId")
    public PromptTemplateDto deactivateVersion(UUID promptId) {
        AiPromptVersion active = versionManager.deactivateVersion(promptId);
        
        if (active != null) {
            AiPromptTemplate template = active.getTemplate();
            template.setStatus(PromptStatus.DRAFT);
            template.setUpdatedAt(LocalDateTime.now());
            AiPromptTemplate saved = templateRepository.save(template);
            return mapToDto(saved);
        }
        
        AiPromptTemplate template = templateRepository.findById(promptId)
                .orElseThrow(() -> new InvalidConversationStateException("Template not found: " + promptId));
        return mapToDto(template);
    }

    @Override
    @Transactional
    @CacheEvict(value = "activePromptVersions", key = "#promptId")
    public PromptTemplateDto rollbackVersion(UUID promptId, Integer versionNumber) {
        AiPromptVersion rolledBack = versionManager.rollbackVersion(promptId, versionNumber);
        
        AiPromptTemplate template = rolledBack.getTemplate();
        template.setStatus(PromptStatus.ACTIVE);
        template.setUpdatedAt(LocalDateTime.now());
        AiPromptTemplate saved = templateRepository.save(template);

        eventPublisher.publishEvent(new PromptActivatedEvent(
                saved.getOrganizationId(),
                promptId,
                versionNumber,
                UUID.randomUUID().toString()
        ));

        return mapToDto(saved);
    }

    @Override
    public List<PromptVersionDto> getVersions(UUID promptId) {
        return versionManager.getVersions(promptId);
    }

    private void validateTenant(UUID orgId) {
        UUID current = TenantContext.getCurrentTenant();
        if (current != null && !current.equals(orgId)) {
            throw new SecurityException("Access Denied: Multitenant boundary restriction.");
        }
    }

    private PromptTemplateDto mapToDto(AiPromptTemplate t) {
        List<PromptVersionDto> versionDtos = versionRepository.findByTemplateId(t.getId()).stream()
                .map(this::mapToVersionDto)
                .collect(Collectors.toList());

        return PromptTemplateDto.builder()
                .id(t.getId())
                .organizationId(t.getOrganizationId())
                .name(t.getName())
                .description(t.getDescription())
                .status(t.getStatus())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .versions(versionDtos)
                .build();
    }

    private PromptVersionDto mapToVersionDto(AiPromptVersion v) {
        return PromptVersionDto.builder()
                .id(v.getId())
                .organizationId(v.getOrganizationId())
                .templateId(v.getTemplate().getId())
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
