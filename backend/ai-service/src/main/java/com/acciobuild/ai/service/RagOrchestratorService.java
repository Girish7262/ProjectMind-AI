package com.acciobuild.ai.service;

import com.acciobuild.ai.orchestrator.ExecutionResult;
import java.util.Map;
import java.util.UUID;

/**
 * Service interface for enterprise AI RAG orchestration and context assembly.
 */
public interface RagOrchestratorService {
    
    /**
     * Triggers the full RAG validation, tool selection, knowledge gathering, ranking, and request compilation pipeline.
     */
    ExecutionResult orchestrate(UUID conversationId, String queryText, Map<String, Object> variables);
}
