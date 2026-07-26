package com.acciobuild.ai.orchestrator;

/**
 * Sequential phases executed by the enterprise RAG orchestration pipeline.
 */
public enum ExecutionStage {
    VALIDATE_REQUEST,
    RESOLVE_CONVERSATION,
    BUILD_CONTEXT,
    RETRIEVE_MEMORY,
    RESOLVE_PROMPT_TEMPLATE,
    DISCOVER_AVAILABLE_TOOLS,
    SELECT_REQUIRED_TOOLS,
    EXECUTE_INTERNAL_TOOLS,
    MERGE_TOOL_RESULTS,
    RANK_RETRIEVED_KNOWLEDGE,
    ASSEMBLE_FINAL_AI_REQUEST,
    SELECT_PROVIDER,
    PRODUCE_EXECUTION_PLAN
}
