package com.acciobuild.ai.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when a new AI model provider is registered in the registry.
 */
@Getter
public class ProviderRegisteredEvent extends AiDomainEvent {
    private final String providerName;
    private final String defaultModel;

    public ProviderRegisteredEvent(UUID organizationId, String providerName, String defaultModel, String correlationId) {
        super("PROVIDER_REGISTERED", organizationId, correlationId);
        this.providerName = providerName;
        this.defaultModel = defaultModel;
    }
}
