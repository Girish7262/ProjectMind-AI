package com.acciobuild.ai.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when routing fails over from a primary provider to a fallback.
 */
@Getter
public class ProviderFallbackEvent extends AiDomainEvent {
    private final String failedProvider;
    private final String fallbackProvider;
    private final String reason;

    public ProviderFallbackEvent(UUID organizationId, String failedProvider, String fallbackProvider, String reason, String correlationId) {
        super("PROVIDER_FALLBACK", organizationId, correlationId);
        this.failedProvider = failedProvider;
        this.fallbackProvider = fallbackProvider;
        this.reason = reason;
    }
}
