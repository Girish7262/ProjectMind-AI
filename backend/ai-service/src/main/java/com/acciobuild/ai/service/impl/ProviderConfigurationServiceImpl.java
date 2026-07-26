package com.acciobuild.ai.service.impl;

import com.acciobuild.ai.domain.model.AiProviderConfiguration;
import com.acciobuild.ai.domain.repository.AiProviderConfigurationRepository;
import com.acciobuild.ai.dto.ProviderConfigurationDto;
import com.acciobuild.ai.enums.ProviderType;
import com.acciobuild.ai.exception.InvalidConversationStateException;
import com.acciobuild.ai.service.ProviderConfigurationService;
import com.acciobuild.ai.multitenancy.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service Implementation managing connection metadata settings for AI model providers.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProviderConfigurationServiceImpl implements ProviderConfigurationService {

    private final AiProviderConfigurationRepository configurationRepository;

    @Override
    @Transactional
    public ProviderConfigurationDto configureProvider(ProviderConfigurationDto dto) {
        log.info("Configuring LLM provider: {}", dto.getConfigName());

        UUID orgId = TenantContext.getCurrentTenant() != null ? TenantContext.getCurrentTenant() : UUID.randomUUID();

        if (Boolean.TRUE.equals(dto.getIsActive())) {
            List<AiProviderConfiguration> existingList = configurationRepository.findAll().stream()
                    .filter(c -> c.getProviderType() == dto.getProviderType() && c.getOrganizationId().equals(orgId))
                    .collect(Collectors.toList());
            for (AiProviderConfiguration cfg : existingList) {
                cfg.setIsActive(false);
                configurationRepository.save(cfg);
            }
        }

        AiProviderConfiguration config = new AiProviderConfiguration();
        config.setId(dto.getId() != null ? dto.getId() : UUID.randomUUID());
        config.setOrganizationId(orgId);
        config.setConfigName(dto.getConfigName());
        config.setProviderType(dto.getProviderType());
        config.setEndpointUrl(dto.getEndpointUrl());
        config.setApiKeyVaultRef(dto.getApiKeyVaultRef());
        config.setDefaultModel(dto.getDefaultModel());
        config.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : false);
        config.setCreatedAt(LocalDateTime.now());
        config.setUpdatedAt(LocalDateTime.now());

        AiProviderConfiguration saved = configurationRepository.save(config);
        return mapToDto(saved);
    }

    @Override
    public ProviderConfigurationDto getActiveProviderConfig(ProviderType type) {
        UUID orgId = TenantContext.getCurrentTenant();
        return configurationRepository.findAll().stream()
                .filter(c -> c.getProviderType() == type && Boolean.TRUE.equals(c.getIsActive()) && (orgId == null || c.getOrganizationId().equals(orgId)))
                .findFirst()
                .map(this::mapToDto)
                .orElseThrow(() -> new InvalidConversationStateException("No active LLM provider configuration found for: " + type));
    }

    @Override
    public List<ProviderConfigurationDto> getConfigs() {
        return configurationRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private ProviderConfigurationDto mapToDto(AiProviderConfiguration config) {
        return ProviderConfigurationDto.builder()
                .id(config.getId())
                .configName(config.getConfigName())
                .providerType(config.getProviderType())
                .endpointUrl(config.getEndpointUrl())
                .apiKeyVaultRef(config.getApiKeyVaultRef())
                .defaultModel(config.getDefaultModel())
                .isActive(config.getIsActive())
                .createdAt(config.getCreatedAt())
                .updatedAt(config.getUpdatedAt())
                .build();
    }
}
