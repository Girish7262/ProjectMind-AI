package com.acciobuild.ai.provider;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Metric tracking model prompt and completion tokens, latency, cost coefficients, and retry attempts.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiTokenUsage implements Serializable {
    private static final long serialVersionUID = 1L;

    private int inputTokens;
    private int outputTokens;
    private double estimatedCost;
    private long latencyMs;
    private int retryCount;
    private String provider;
    private String model;

    public int getTotalTokens() {
        return inputTokens + outputTokens;
    }
}
