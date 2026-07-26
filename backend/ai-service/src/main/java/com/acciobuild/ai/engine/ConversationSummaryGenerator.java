package com.acciobuild.ai.engine;

import com.acciobuild.ai.domain.model.AiConversationMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * Stub Generator creating summaries of historical messages to compress prompt size.
 */
@Component
@Slf4j
public class ConversationSummaryGenerator {

    /**
     * Generates a summary string for a list of conversation messages.
     */
    public String generateSummary(List<AiConversationMessage> messages) {
        log.info("Generating summary stub for {} conversation messages", messages.size());
        if (messages.isEmpty()) {
            return "No previous conversation context.";
        }
        return "Summary of previous discussion: The user requested help with AccioBuild enterprise platform, project workspaces, and RAG configuration details.";
    }
}
