package com.acciobuild.ai.service;

import com.acciobuild.ai.dto.ProviderConfigurationDto;
import com.acciobuild.ai.enums.ProviderType;
import java.util.List;
import java.util.UUID;

/**
 * Service Contract for managing dynamic LLM provider settings configurations.
 */
public interface ProviderConfigurationService {
    ProviderConfigurationDto configureProvider(ProviderConfigurationDto dto);
    ProviderConfigurationDto getActiveProviderConfig(ProviderType type);
    List<ProviderConfigurationDto> getConfigs();
}
