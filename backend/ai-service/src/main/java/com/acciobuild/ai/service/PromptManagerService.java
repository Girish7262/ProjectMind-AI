package com.acciobuild.ai.service;

import com.acciobuild.ai.dto.PromptTemplateDto;
import com.acciobuild.ai.dto.PromptVersionDto;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for enterprise AI Prompt Template management.
 */
public interface PromptManagerService {
    PromptTemplateDto createPrompt(PromptTemplateDto dto);
    PromptTemplateDto updatePrompt(UUID promptId, PromptTemplateDto dto);
    PromptTemplateDto getPrompt(UUID promptId);
    PromptTemplateDto clonePrompt(UUID promptId);
    void archivePrompt(UUID promptId);
    void restorePrompt(UUID promptId);
    void deletePrompt(UUID promptId);
    PromptVersionDto createVersion(UUID promptId, PromptVersionDto versionDto);
    PromptTemplateDto activateVersion(UUID promptId, Integer versionNumber);
    PromptTemplateDto deactivateVersion(UUID promptId);
    PromptTemplateDto rollbackVersion(UUID promptId, Integer versionNumber);
    List<PromptVersionDto> getVersions(UUID promptId);
}
