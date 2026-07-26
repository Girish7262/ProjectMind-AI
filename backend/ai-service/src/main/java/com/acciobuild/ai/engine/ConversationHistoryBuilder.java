package com.acciobuild.ai.engine;

import com.acciobuild.ai.domain.model.AiConversationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * Utility compiling list of conversation messages into structured prompt context.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ConversationHistoryBuilder {

    private final ConversationTokenEstimator tokenEstimator;

    /**
     * Builds a single formatted history string from a list of conversation messages.
     */
    public String buildHistoryText(List<AiConversationMessage> messages) {
        log.info("Compiling history text for {} messages", messages.size());
        StringBuilder builder = new StringBuilder();
        for (AiConversationMessage msg : messages) {
            builder.append(msg.getRole().name())
                    .append(": ")
                    .append(msg.getContent())
                    .append("\n");
        }
        return builder.toString();
    }
}
