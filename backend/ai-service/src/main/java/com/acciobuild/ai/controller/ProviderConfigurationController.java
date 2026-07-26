package com.acciobuild.ai.controller;

import com.acciobuild.ai.dto.ProviderConfigurationDto;
import com.acciobuild.ai.enums.ProviderType;
import com.acciobuild.ai.service.ProviderConfigurationService;
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
import java.util.List;

/**
 * REST Controller exposing AI LLM Provider Configuration endpoints.
 */
@RestController
@RequestMapping("/api/v1/ai/providers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Provider Configurations", description = "AI LLM Provider connections and credentials configurations endpoints")
public class ProviderConfigurationController {

    private final ProviderConfigurationService providerConfigurationService;

    /**
     * Configures/provisions connection credentials for a provider.
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('OWNER', 'ADMIN')")
    @Operation(summary = "Configure provider settings", description = "Configures/provisions connection credentials for a provider")
    public ResponseEntity<ApiResponse<ProviderConfigurationDto>> configure(
            @Valid @RequestBody ProviderConfigurationDto dto) {
        log.info("REST: Request to configure provider: {}", dto.getConfigName());
        ProviderConfigurationDto result = providerConfigurationService.configureProvider(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(201, "LLM provider settings configured successfully", result));
    }

    /**
     * Lists active model configuration rules details of a provider.
     */
    @GetMapping("/active/{type}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get active provider configuration", description = "Lists active model configuration rules details of a provider")
    public ResponseEntity<ApiResponse<ProviderConfigurationDto>> getActive(@PathVariable("type") ProviderType type) {
        log.info("REST: Request to fetch active config for provider: {}", type);
        ProviderConfigurationDto dto = providerConfigurationService.getActiveProviderConfig(type);
        return ResponseEntity.ok(new ApiResponse<>(200, "Active configuration rules loaded", dto));
    }

    /**
     * Lists all registered LLM provider credentials configurations.
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('OWNER', 'ADMIN')")
    @Operation(summary = "List provider configurations", description = "Lists all registered LLM provider credentials configurations")
    public ResponseEntity<ApiResponse<List<ProviderConfigurationDto>>> list() {
        log.info("REST: Request to list all LLM provider configurations.");
        List<ProviderConfigurationDto> list = providerConfigurationService.getConfigs();
        return ResponseEntity.ok(new ApiResponse<>(200, "Configurations list retrieved successfully", list));
    }
}
