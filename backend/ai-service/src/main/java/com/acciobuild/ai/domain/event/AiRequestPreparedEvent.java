package com.acciobuild.ai.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when an LLM provider request payload is prepared.
 */
@Getter
public class AiRequestPreparedEvent extends AiDomainEvent {
    private final String providerName;
    private final String modelName;
    private final double temperature;

    public AiRequestPreparedEvent(UUID organizationId, String providerName, String modelName, double temperature, String correlationId) {
        super("AI_REQUEST_PREPARED", organizationId, correlationId);
        this.providerName = providerName;
        this.modelName = modelName;
        this.temperature = temperature;
    }
}
