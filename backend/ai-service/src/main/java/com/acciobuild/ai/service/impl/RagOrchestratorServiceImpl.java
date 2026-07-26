package com.acciobuild.ai.service.impl;

import com.acciobuild.ai.domain.event.*;
import com.acciobuild.ai.domain.model.AiConversation;
import com.acciobuild.ai.domain.repository.AiConversationRepository;
import com.acciobuild.ai.dto.ContextDto;
import com.acciobuild.ai.dto.ConversationDto;
import com.acciobuild.ai.enums.ConversationStatus;
import com.acciobuild.ai.enums.ProviderType;
import com.acciobuild.ai.exception.ConversationNotFoundException;
import com.acciobuild.ai.exception.InvalidConversationStateException;
import com.acciobuild.ai.multitenancy.TenantContext;
import com.acciobuild.ai.orchestrator.ExecutionPlan;
import com.acciobuild.ai.orchestrator.ExecutionResult;
import com.acciobuild.ai.orchestrator.ExecutionStage;
import com.acciobuild.ai.provider.AiProvider;
import com.acciobuild.ai.provider.AiProviderRegistry;
import com.acciobuild.ai.provider.strategy.PrimarySelectionStrategy;
import com.acciobuild.ai.service.ContextBuilderService;
import com.acciobuild.ai.service.MemoryManagerService;
import com.acciobuild.ai.service.RagOrchestratorService;
import com.acciobuild.ai.tool.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Core orchestrator executing validation, context assembly, tool execution,
 * ranking, and compiled execution plan generation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RagOrchestratorServiceImpl implements RagOrchestratorService {

    private final AiConversationRepository conversationRepository;
    private final ContextBuilderService contextBuilderService;
    private final MemoryManagerService memoryManagerService;
    private final ToolRegistry toolRegistry;
    private final ToolSelector toolSelector;
    private final ToolExecutionEngine toolExecutionEngine;
    private final AiProviderRegistry providerRegistry;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    @Cacheable(value = "executionPlans", key = "#conversationId")
    public ExecutionResult orchestrate(UUID conversationId, String queryText, Map<String, Object> variables) {
        log.info("Starting RAG Orchestration pipeline for conversation: {}", conversationId);
        long startTime = System.currentTimeMillis();
        String correlationId = UUID.randomUUID().toString();

        UUID tenantId = TenantContext.getCurrentTenant() != null ? TenantContext.getCurrentTenant() : UUID.randomUUID();
        eventPublisher.publishEvent(new ExecutionStartedEvent(tenantId, conversationId, queryText, correlationId));

        List<ExecutionStage> completed = new ArrayList<>();
        Map<ExecutionStage, Long> stageLatencies = new LinkedHashMap<>();

        try {
            // Stage 1: Validate Request
            long stageStart = System.currentTimeMillis();
            if (conversationId == null || queryText == null || queryText.strip().isEmpty()) {
                throw new IllegalArgumentException("Invalid RAG payload: conversationId and queryText are required.");
            }
            recordStage(completed, stageLatencies, ExecutionStage.VALIDATE_REQUEST, stageStart, tenantId, conversationId, correlationId);

            // Stage 2: Resolve Conversation
            stageStart = System.currentTimeMillis();
            AiConversation conv = conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new ConversationNotFoundException("Conversation not found for RAG: " + conversationId));
            validateOwnership(conv);
            if (conv.getStatus() != ConversationStatus.ACTIVE) {
                throw new InvalidConversationStateException("Cannot orchestrate a pipeline on non-active conversation.");
            }
            recordStage(completed, stageLatencies, ExecutionStage.RESOLVE_CONVERSATION, stageStart, tenantId, conversationId, correlationId);

            // Stage 3: Build Context
            stageStart = System.currentTimeMillis();
            ContextDto contextDto = contextBuilderService.buildContext(conversationId, queryText);
            recordStage(completed, stageLatencies, ExecutionStage.BUILD_CONTEXT, stageStart, tenantId, conversationId, correlationId);

            // Stage 4: Retrieve Memory
            stageStart = System.currentTimeMillis();
            Map<String, String> memories = memoryManagerService.getMemories(conversationId).stream()
                    .collect(Collectors.toMap(m -> m.getMemoryKey(), m -> m.getMemoryValue() != null ? m.getMemoryValue() : "", (a, b) -> a));
            recordStage(completed, stageLatencies, ExecutionStage.RETRIEVE_MEMORY, stageStart, tenantId, conversationId, correlationId);

            // Stage 5: Resolve Prompt Template
            stageStart = System.currentTimeMillis();
            String resolvedPrompt = "System Prompt Instruction set.\nUser payload context: " + queryText;
            recordStage(completed, stageLatencies, ExecutionStage.RESOLVE_PROMPT_TEMPLATE, stageStart, tenantId, conversationId, correlationId);

            // Stage 6: Discover Available Tools
            stageStart = System.currentTimeMillis();
            List<Tool> allTools = toolRegistry.getAllTools();
            recordStage(completed, stageLatencies, ExecutionStage.DISCOVER_AVAILABLE_TOOLS, stageStart, tenantId, conversationId, correlationId);

            // Stage 7: Select Required Tools
            stageStart = System.currentTimeMillis();
            List<Tool> selectedTools = toolSelector.selectTools(queryText);
            recordStage(completed, stageLatencies, ExecutionStage.SELECT_REQUIRED_TOOLS, stageStart, tenantId, conversationId, correlationId);

            // Stage 8: Execute Internal Tools
            stageStart = System.currentTimeMillis();
            ToolExecutionContext toolCtx = ToolExecutionContext.builder()
                    .conversationId(conversationId)
                    .organizationId(conv.getOrganizationId())
                    .traceId(correlationId)
                    .build();
            Map<String, Map<String, Object>> toolArgs = new HashMap<>();
            // Populate tool arguments
            for (Tool t : selectedTools) {
                Map<String, Object> args = new HashMap<>();
                args.put("query", queryText);
                args.put("conversationId", conversationId.toString());
                args.put("organizationId", conv.getOrganizationId().toString());
                toolArgs.put(t.getName(), args);
            }
            Map<String, Object> toolResults = toolExecutionEngine.executeToolsParallel(selectedTools, toolArgs, toolCtx);
            recordStage(completed, stageLatencies, ExecutionStage.EXECUTE_INTERNAL_TOOLS, stageStart, tenantId, conversationId, correlationId);

            // Stage 9: Merge Tool Results
            stageStart = System.currentTimeMillis();
            List<Map<String, Object>> mergedChunks = new ArrayList<>();
            Object searchOutcome = toolResults.get("Knowledge Search");
            if (searchOutcome instanceof List<?> list) {
                for (Object item : list) {
                    if (item instanceof Map<?, ?> m) {
                        mergedChunks.add((Map<String, Object>) m);
                    }
                }
            }
            recordStage(completed, stageLatencies, ExecutionStage.MERGE_TOOL_RESULTS, stageStart, tenantId, conversationId, correlationId);

            // Stage 10: Rank Retrieved Knowledge
            stageStart = System.currentTimeMillis();
            List<Map<String, Object>> rankedKnowledge = rankKnowledge(mergedChunks);
            eventPublisher.publishEvent(new KnowledgeRetrievedEvent(tenantId, conversationId, rankedKnowledge.size(), correlationId));
            recordStage(completed, stageLatencies, ExecutionStage.RANK_RETRIEVED_KNOWLEDGE, stageStart, tenantId, conversationId, correlationId);

            // Stage 11: Assemble Final AI Request
            stageStart = System.currentTimeMillis();
            StringBuilder promptBuilder = new StringBuilder(resolvedPrompt);
            if (!rankedKnowledge.isEmpty()) {
                promptBuilder.append("\n\nRanked Knowledge Sources:\n");
                for (Map<String, Object> chunk : rankedKnowledge) {
                    promptBuilder.append("- ").append(chunk.get("content")).append("\n");
                }
            }
            recordStage(completed, stageLatencies, ExecutionStage.ASSEMBLE_FINAL_AI_REQUEST, stageStart, tenantId, conversationId, correlationId);

            // Stage 12: Select Provider
            stageStart = System.currentTimeMillis();
            List<AiProvider> activeProviders = providerRegistry.getActiveProviders();
            PrimarySelectionStrategy selectStrategy = new PrimarySelectionStrategy();
            AiProvider chosenProvider = selectStrategy.selectProvider(activeProviders, null);
            ProviderType providerType = chosenProvider != null ? chosenProvider.getType() : ProviderType.OPENAI;
            String modelName = chosenProvider != null ? chosenProvider.getDiscoverableModels().get(0) : "gpt-4o";
            recordStage(completed, stageLatencies, ExecutionStage.SELECT_PROVIDER, stageStart, tenantId, conversationId, correlationId);

            // Stage 13: Produce Execution Plan
            stageStart = System.currentTimeMillis();
            ConversationDto convDto = ConversationDto.builder()
                    .id(conv.getId())
                    .projectId(conv.getProjectId())
                    .title(conv.getTitle())
                    .organizationId(conv.getOrganizationId())
                    .status(conv.getStatus())
                    .build();

            ExecutionPlan plan = ExecutionPlan.builder()
                    .planId(UUID.randomUUID())
                    .conversationId(conversationId)
                    .conversation(convDto)
                    .context(contextDto)
                    .compiledPrompt(promptBuilder.toString())
                    .memoryVariables(memories)
                    .retrievedKnowledge(rankedKnowledge)
                    .selectedProvider(providerType)
                    .selectedModel(modelName)
                    .selectedTools(selectedTools.stream().map(Tool::getName).collect(Collectors.toList()))
                    .estimatedTokens(1500)
                    .metadata(Map.of("correlationId", correlationId, "createdAt", LocalDateTime.now().toString()))
                    .build();

            eventPublisher.publishEvent(new ExecutionPlanCreatedEvent(tenantId, conversationId, plan.getPlanId(), providerType.name(), modelName, correlationId));
            recordStage(completed, stageLatencies, ExecutionStage.PRODUCE_EXECUTION_PLAN, stageStart, tenantId, conversationId, correlationId);

            long totalDuration = System.currentTimeMillis() - startTime;
            eventPublisher.publishEvent(new ExecutionCompletedEvent(tenantId, conversationId, totalDuration, correlationId));

            return ExecutionResult.builder()
                    .plan(plan)
                    .successful(true)
                    .completedStages(completed)
                    .stageLatencies(stageLatencies)
                    .totalDurationMs(totalDuration)
                    .build();

        } catch (Exception e) {
            log.error("RAG Orchestration failed for conversation {}: {}", conversationId, e.getMessage(), e);
            long totalDuration = System.currentTimeMillis() - startTime;
            String failingStage = completed.isEmpty() ? "START" : completed.get(completed.size() - 1).name();
            eventPublisher.publishEvent(new ExecutionFailedEvent(tenantId, conversationId, failingStage, e.getMessage(), correlationId));
            
            return ExecutionResult.builder()
                    .successful(false)
                    .errorMessage(e.getMessage())
                    .completedStages(completed)
                    .stageLatencies(stageLatencies)
                    .totalDurationMs(totalDuration)
                    .build();
        }
    }

    private void recordStage(List<ExecutionStage> completed, Map<ExecutionStage, Long> latencies, ExecutionStage stage, long stageStart, UUID tenantId, UUID conversationId, String correlationId) {
        long duration = System.currentTimeMillis() - stageStart;
        completed.add(stage);
        latencies.put(stage, duration);
        eventPublisher.publishEvent(new ExecutionStageCompletedEvent(tenantId, conversationId, stage.name(), duration, correlationId));
    }

    private List<Map<String, Object>> rankKnowledge(List<Map<String, Object>> rawChunks) {
        List<Map<String, Object>> ranked = new ArrayList<>(rawChunks);
        for (Map<String, Object> chunk : ranked) {
            double baseScore = getDouble(chunk, "score", 0.7);
            double keywordScore = getDouble(chunk, "keywordScore", 0.6);
            double recencyBoost = "new".equalsIgnoreCase(getString(chunk, "freshness")) ? 0.15 : 0.05;
            double manualBoost = getDouble(chunk, "manualBoost", 0.0);
            boolean pinned = getBoolean(chunk, "pinned");

            double composite = baseScore * 0.4 + keywordScore * 0.3 + recencyBoost + manualBoost;
            if (pinned) {
                composite += 10.0;
            }
            chunk.put("compositeScore", composite);
        }
        ranked.sort((c1, c2) -> Double.compare(getDouble(c2, "compositeScore", 0.0), getDouble(c1, "compositeScore", 0.0)));
        return ranked;
    }

    private double getDouble(Map<String, Object> map, String key, double defaultVal) {
        Object val = map.get(key);
        if (val instanceof Number num) {
            return num.doubleValue();
        }
        return defaultVal;
    }

    private boolean getBoolean(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val instanceof Boolean b) {
            return b;
        }
        return false;
    }

    private String getString(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val != null) {
            return val.toString();
        }
        return "";
    }

    private void validateOwnership(AiConversation conv) {
        UUID currentTenant = TenantContext.getCurrentTenant();
        if (currentTenant != null && !currentTenant.equals(conv.getOrganizationId())) {
            throw new SecurityException("Tenant isolation boundary violation: operation denied.");
        }
    }
}
