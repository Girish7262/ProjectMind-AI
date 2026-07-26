package com.acciobuild.ai.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when an AI provider configuration is activated.
 */
@Getter
public class ProviderActivatedEvent extends AiDomainEvent {
    private final String providerName;

    public ProviderActivatedEvent(UUID organizationId, String providerName, String correlationId) {
        super("PROVIDER_ACTIVATED", organizationId, correlationId);
        this.providerName = providerName;
    }
}
