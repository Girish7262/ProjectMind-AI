package com.acciobuild.ai.engine;

import com.acciobuild.ai.dto.ContextDto;
import java.util.UUID;

/**
 * Service Contract for AI Conversation Context and History assembly operations.
 */
public interface ConversationEngineService {

    /**
     * Compiles RAG context sources, filters history logs, estimates tokens, and validates tenant context rules.
     */
    ContextDto assembleConversationContext(UUID conversationId, String queryText);
}
