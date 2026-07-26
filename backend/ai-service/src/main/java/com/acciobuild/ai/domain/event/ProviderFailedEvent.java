package com.acciobuild.ai.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when a provider invocation fails.
 */
@Getter
public class ProviderFailedEvent extends AiDomainEvent {
    private final String providerName;
    private final String errorMessage;

    public ProviderFailedEvent(UUID organizationId, String providerName, String errorMessage, String correlationId) {
        super("PROVIDER_FAILED", organizationId, correlationId);
        this.providerName = providerName;
        this.errorMessage = errorMessage;
    }
}
