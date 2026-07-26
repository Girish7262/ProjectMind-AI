package com.acciobuild.ai.engine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Utility Estimator approximating token consumption mapping character counts.
 * Uses standard 4 characters per token ratio to avoid heavy runtime tokenizer dependencies.
 */
@Component
@Slf4j
public class ConversationTokenEstimator {

    private static final int CHARACTERS_PER_TOKEN = 4;

    /**
     * Estimates token count for a text string.
     */
    public int estimateTokens(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        int tokens = (text.length() + CHARACTERS_PER_TOKEN - 1) / CHARACTERS_PER_TOKEN;
        log.debug("Estimated {} tokens for text payload length {}", tokens, text.length());
        return tokens;
    }
}
