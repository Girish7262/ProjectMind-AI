package com.acciobuild.ai.engine;

import com.acciobuild.ai.domain.model.AiConversationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility managing sliding conversation windows, preserving pinned messages, and pruning older messages.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ConversationWindowManager {

    private final ConversationTokenEstimator tokenEstimator;

    /**
     * Filters conversation messages history to fit within max token budgets, keeping pinned items.
     */
    public List<AiConversationMessage> getWindow(List<AiConversationMessage> history, int maxTokens) {
        log.info("Processing sliding conversation window. Max token budget: {}", maxTokens);

        List<AiConversationMessage> result = new ArrayList<>();
        int currentTokens = 0;

        // Pinned/critical messages are preserved first
        for (AiConversationMessage msg : history) {
            if (isPinned(msg)) {
                int tokens = tokenEstimator.estimateTokens(msg.getContent());
                currentTokens += tokens;
                result.add(msg);
            }
        }

        // Process remaining messages starting from the most recent (recent first)
        for (int i = history.size() - 1; i >= 0; i--) {
            AiConversationMessage msg = history.get(i);
            if (isPinned(msg)) {
                continue;
            }

            int tokens = tokenEstimator.estimateTokens(msg.getContent());
            if (currentTokens + tokens <= maxTokens) {
                currentTokens += tokens;
                result.add(0, msg); // Prepend to maintain chronological order
            } else {
                log.debug("Message ID {} dropped due to token budget limits", msg.getId());
            }
        }

        return result;
    }

    private boolean isPinned(AiConversationMessage msg) {
        // Mock check for pinned/important flags
        return msg.getTotalTokens() != null && msg.getTotalTokens() > 1000;
    }
}
