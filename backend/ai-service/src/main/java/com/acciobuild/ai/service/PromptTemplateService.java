package com.acciobuild.ai.service;

import com.acciobuild.ai.dto.PromptTemplateDto;
import com.acciobuild.ai.dto.PromptVersionDto;
import java.util.UUID;

/**
 * Service Contract for Dynamic Prompt Templates revisions management.
 */
public interface PromptTemplateService {
    PromptTemplateDto createTemplate(PromptTemplateDto dto);
    PromptTemplateDto getTemplateByName(String name);
    PromptVersionDto addVersion(UUID templateId, PromptVersionDto versionDto);
    PromptTemplateDto activateVersion(UUID templateId, Integer versionNumber);
}
