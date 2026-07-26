package com.acciobuild.knowledge.ai.impl;

import com.acciobuild.knowledge.ai.TokenEstimator;
import org.springframework.stereotype.Component;

/**
 * Stub implementation of TokenEstimator for offline/asynchronous pipelines.
 */
@Component
public class StubTokenEstimator implements TokenEstimator {

    @Override
    public int estimateTokens(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        // Basic approximation: 1 token is roughly 4 characters or 0.75 words.
        // We will split by whitespace as a robust stub approximation.
        return text.split("\\s+").length;
    }

    @Override
    public double calculateCost(String text, String model) {
        int tokens = estimateTokens(text);
        // Base rate: $0.0001 per 1000 tokens
        return (tokens / 1000.0) * 0.0001;
    }
}
