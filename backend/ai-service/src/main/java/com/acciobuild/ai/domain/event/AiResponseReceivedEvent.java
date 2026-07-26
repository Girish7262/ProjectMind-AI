package com.acciobuild.ai.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when an LLM provider response is parsed and received.
 */
@Getter
public class AiResponseReceivedEvent extends AiDomainEvent {
    private final String providerName;
    private final String modelName;
    private final int promptTokens;
    private final int completionTokens;
    private final long latencyMs;

    public AiResponseReceivedEvent(UUID organizationId, String providerName, String modelName, int promptTokens, int completionTokens, long latencyMs, String correlationId) {
        super("AI_RESPONSE_RECEIVED", organizationId, correlationId);
        this.providerName = providerName;
        this.modelName = modelName;
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
        this.latencyMs = latencyMs;
    }
}
