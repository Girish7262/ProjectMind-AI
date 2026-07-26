package com.acciobuild.ai.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when an AI provider configuration is deactivated.
 */
@Getter
public class ProviderDeactivatedEvent extends AiDomainEvent {
    private final String providerName;

    public ProviderDeactivatedEvent(UUID organizationId, String providerName, String correlationId) {
        super("PROVIDER_DEACTIVATED", organizationId, correlationId);
        this.providerName = providerName;
    }
}
