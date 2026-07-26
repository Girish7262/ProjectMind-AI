package com.acciobuild.ai.controller;

import com.acciobuild.ai.dto.PromptTemplateDto;
import com.acciobuild.ai.dto.PromptVersionDto;
import com.acciobuild.ai.service.PromptTemplateService;
import com.acciobuild.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

/**
 * REST Controller exposing AI Prompt Templates management endpoints.
 */
@RestController
@RequestMapping("/api/v1/ai/prompts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Prompt Templates", description = "AI Prompt Templates management endpoints")
public class PromptTemplateController {

    private final PromptTemplateService promptTemplateService;

    /**
     * Provisions a new Prompt Template draft.
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('OWNER', 'MAINTAINER', 'EDITOR')")
    @Operation(summary = "Create template draft", description = "Provisions a new Prompt Template draft")
    public ResponseEntity<ApiResponse<PromptTemplateDto>> create(@Valid @RequestBody PromptTemplateDto dto) {
        log.info("REST: Request to create prompt template: {}", dto.getName());
        PromptTemplateDto result = promptTemplateService.createTemplate(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(201, "Prompt template draft registered successfully", result));
    }

    /**
     * Creates a new immutable template version.
     */
    @PostMapping("/{templateId}/versions")
    @PreAuthorize("hasAnyAuthority('OWNER', 'MAINTAINER', 'EDITOR')")
    @Operation(summary = "Add version revision", description = "Appends a new immutable template version revision")
    public ResponseEntity<ApiResponse<PromptVersionDto>> addVersion(
            @PathVariable("templateId") UUID templateId,
            @Valid @RequestBody PromptVersionDto versionDto) {
        log.info("REST: Request to add version for template: {}", templateId);
        PromptVersionDto result = promptTemplateService.addVersion(templateId, versionDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(201, "Prompt version revision added successfully", result));
    }

    /**
     * Sets a specific version of a template as the active default.
     */
    @PostMapping("/{templateId}/activate/{versionNumber}")
    @PreAuthorize("hasAnyAuthority('OWNER', 'MAINTAINER', 'EDITOR')")
    @Operation(summary = "Activate version default", description = "Sets a specific version of a template as the active default")
    public ResponseEntity<ApiResponse<PromptTemplateDto>> activateVersion(
            @PathVariable("templateId") UUID templateId,
            @PathVariable("versionNumber") Integer versionNumber) {
        log.info("REST: Request to activate version {} for template: {}", versionNumber, templateId);
        PromptTemplateDto result = promptTemplateService.activateVersion(templateId, versionNumber);
        return ResponseEntity.ok(new ApiResponse<>(200, "Version activated successfully", result));
    }

    /**
     * Retrieves details of a template using its unique name.
     */
    @GetMapping("/name/{name}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Retrieve template by name", description = "Retrieves details of a template using its unique name")
    public ResponseEntity<ApiResponse<PromptTemplateDto>> getByName(@PathVariable("name") String name) {
        log.info("REST: Request to get prompt template by name: {}", name);
        PromptTemplateDto dto = promptTemplateService.getTemplateByName(name);
        return ResponseEntity.ok(new ApiResponse<>(200, "Template details retrieved", dto));
    }
}
