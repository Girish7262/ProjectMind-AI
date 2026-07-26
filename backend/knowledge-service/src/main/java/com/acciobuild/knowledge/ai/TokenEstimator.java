package com.acciobuild.knowledge.ai;

/**
 * Service to estimate prompt/chunk tokens and track predicted pricing.
 */
public interface TokenEstimator {

    /**
     * Compute approximate token count for a text segment.
     */
    int estimateTokens(String text);

    /**
     * Calculate cost projection based on token estimate and selected model name.
     */
    double calculateCost(String text, String model);
}
