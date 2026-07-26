package com.acciobuild.ai.prompt;

import com.acciobuild.ai.domain.model.AiPromptTemplate;
import com.acciobuild.ai.domain.model.AiPromptVersion;
import com.acciobuild.ai.domain.repository.AiPromptTemplateRepository;
import com.acciobuild.ai.domain.repository.AiPromptVersionRepository;
import com.acciobuild.ai.dto.PromptVersionDto;
import com.acciobuild.ai.exception.InvalidConversationStateException;
import com.acciobuild.ai.multitenancy.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manager handling prompt version snapshot creation, rollback triggers, and detailed comparisons.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PromptVersionManager {

    private final AiPromptVersionRepository versionRepository;
    private final AiPromptTemplateRepository templateRepository;
    private final PromptValidator promptValidator;

    @Transactional
    public PromptVersionDto createVersion(UUID templateId, PromptVersionDto versionDto) {
        log.info("Creating immutable version for template: {}", templateId);
        
        AiPromptTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new InvalidConversationStateException("Template not found: " + templateId));
        
        validateTenant(template.getOrganizationId());

        List<AiPromptVersion> versions = versionRepository.findByTemplateId(templateId);
        int nextVersionNumber = versions.stream()
                .mapToInt(AiPromptVersion::getVersionNumber)
                .max()
                .orElse(0) + 1;

        AiPromptVersion version = new AiPromptVersion();
        version.setId(UUID.randomUUID());
        version.setOrganizationId(template.getOrganizationId());
        version.setTemplate(template);
        version.setVersionNumber(nextVersionNumber);
        version.setSystemInstruction(versionDto.getSystemInstruction());
        version.setUserTemplate(versionDto.getUserTemplate());
        version.setParametersJson(versionDto.getParametersJson());
        version.setIsActive(false);
        version.setCreatedBy(versionDto.getCreatedBy() != null ? versionDto.getCreatedBy() : UUID.randomUUID());
        version.setCreatedAt(LocalDateTime.now());

        promptValidator.validateVersion(version);

        AiPromptVersion saved = versionRepository.save(version);
        return mapToVersionDto(saved);
    }

    @Transactional
    public AiPromptVersion activateVersion(UUID templateId, Integer versionNumber) {
        log.info("Activating version {} for template {}", versionNumber, templateId);

        AiPromptTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new InvalidConversationStateException("Template not found: " + templateId));

        validateTenant(template.getOrganizationId());

        List<AiPromptVersion> versions = versionRepository.findByTemplateId(templateId);
        AiPromptVersion target = versions.stream()
                .filter(v -> v.getVersionNumber().equals(versionNumber))
                .findFirst()
                .orElseThrow(() -> new InvalidConversationStateException("Version number " + versionNumber + " not found."));

        for (AiPromptVersion v : versions) {
            v.setIsActive(v.getId().equals(target.getId()));
            versionRepository.save(v);
        }

        return target;
    }

    @Transactional
    public AiPromptVersion deactivateVersion(UUID templateId) {
        log.info("Deactivating all versions for template {}", templateId);

        AiPromptTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new InvalidConversationStateException("Template not found: " + templateId));

        validateTenant(template.getOrganizationId());

        List<AiPromptVersion> versions = versionRepository.findByTemplateId(templateId);
        AiPromptVersion active = null;
        for (AiPromptVersion v : versions) {
            if (Boolean.TRUE.equals(v.getIsActive())) {
                v.setIsActive(false);
                versionRepository.save(v);
                active = v;
            }
        }
        return active;
    }

    @Transactional
    public AiPromptVersion rollbackVersion(UUID templateId, Integer versionNumber) {
        log.info("Rolling back template {} to version {}", templateId, versionNumber);
        return activateVersion(templateId, versionNumber);
    }

    public Map<String, Object> compareVersions(UUID templateId, Integer v1Number, Integer v2Number) {
        log.info("Comparing versions {} and {} for template {}", v1Number, v2Number, templateId);
        
        List<AiPromptVersion> versions = versionRepository.findByTemplateId(templateId);
        
        AiPromptVersion v1 = versions.stream()
                .filter(v -> v.getVersionNumber().equals(v1Number))
                .findFirst()
                .orElseThrow(() -> new InvalidConversationStateException("Version " + v1Number + " not found."));
                
        AiPromptVersion v2 = versions.stream()
                .filter(v -> v.getVersionNumber().equals(v2Number))
                .findFirst()
                .orElseThrow(() -> new InvalidConversationStateException("Version " + v2Number + " not found."));

        validateTenant(v1.getOrganizationId());

        Map<String, Object> comparison = new LinkedHashMap<>();
        comparison.put("templateId", templateId);
        comparison.put("v1", v1Number);
        comparison.put("v2", v2Number);
        
        comparison.put("systemInstructionChanged", !Objects.equals(v1.getSystemInstruction(), v2.getSystemInstruction()));
        comparison.put("userTemplateChanged", !Objects.equals(v1.getUserTemplate(), v2.getUserTemplate()));
        comparison.put("parametersJsonChanged", !Objects.equals(v1.getParametersJson(), v2.getParametersJson()));
        
        comparison.put("systemInstructionDiff", getDiffSummary(v1.getSystemInstruction(), v2.getSystemInstruction()));
        comparison.put("userTemplateDiff", getDiffSummary(v1.getUserTemplate(), v2.getUserTemplate()));
        
        return comparison;
    }

    private String getDiffSummary(String s1, String s2) {
        if (s1 == null) s1 = "";
        if (s2 == null) s2 = "";
        if (s1.equals(s2)) {
            return "No changes.";
        }
        return String.format("Length changed from %d to %d characters.", s1.length(), s2.length());
    }

    public List<PromptVersionDto> getVersions(UUID templateId) {
        AiPromptTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new InvalidConversationStateException("Template not found: " + templateId));

        validateTenant(template.getOrganizationId());

        return versionRepository.findByTemplateId(templateId).stream()
                .map(this::mapToVersionDto)
                .collect(Collectors.toList());
    }

    private void validateTenant(UUID orgId) {
        UUID current = TenantContext.getCurrentTenant();
        if (current != null && !current.equals(orgId)) {
            throw new SecurityException("Access Denied: Multitenant boundary restriction.");
        }
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
