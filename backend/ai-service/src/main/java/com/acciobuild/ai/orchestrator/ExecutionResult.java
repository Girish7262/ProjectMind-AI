package com.acciobuild.ai.orchestrator;

import lombok.Builder;
import lombok.Value;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Result metrics and status summary returned by the RAG Orchestrator execution engine.
 */
@Value
@Builder
public class ExecutionResult implements Serializable {
    private static final long serialVersionUID = 1L;

    ExecutionPlan plan;
    boolean successful;
    List<ExecutionStage> completedStages;
    Map<ExecutionStage, Long> stageLatencies;
    String errorMessage;
    long totalDurationMs;
}
