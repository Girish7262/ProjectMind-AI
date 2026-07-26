package com.acciobuild.ai.orchestrator;

import com.acciobuild.ai.dto.ContextDto;
import com.acciobuild.ai.dto.ConversationDto;
import com.acciobuild.ai.enums.ProviderType;
import lombok.Builder;
import lombok.Value;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Immutable model containing the outputs compiled by the RAG orchestration pipeline.
 */
@Value
@Builder
public class ExecutionPlan implements Serializable {
    private static final long serialVersionUID = 1L;

    UUID planId;
    UUID conversationId;
    ConversationDto conversation;
    ContextDto context;
    String compiledPrompt;
    Map<String, String> memoryVariables;
    List<Map<String, Object>> retrievedKnowledge;
    ProviderType selectedProvider;
    String selectedModel;
    List<String> selectedTools;
    int estimatedTokens;
    Map<String, Object> metadata;
}
