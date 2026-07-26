package com.acciobuild.ai.dto;

import com.acciobuild.ai.enums.ProviderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object mapping LLM backend settings.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderConfigurationDto {
    private UUID id;
    private UUID organizationId;
    private ProviderType providerType;
    private String configName;
    private String endpointUrl;
    private String apiKeyVaultRef;
    private String defaultModel;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
