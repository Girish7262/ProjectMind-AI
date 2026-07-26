package com.acciobuild.ai.tool;

import com.acciobuild.ai.domain.event.ToolExecutionCompletedEvent;
import com.acciobuild.ai.domain.event.ToolExecutionStartedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.*;

/**
 * Execution engine facilitating sequential and parallel routing, retries, and timeouts.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ToolExecutionEngine {

    private final ApplicationEventPublisher eventPublisher;

    public Map<String, Object> executeToolsSequential(List<Tool> tools, Map<String, Map<String, Object>> toolArgs, ToolExecutionContext context) {
        log.info("Executing tools sequentially: count={}", tools.size());
        Map<String, Object> results = new LinkedHashMap<>();

        for (Tool tool : tools) {
            Map<String, Object> args = toolArgs.getOrDefault(tool.getName(), new HashMap<>());
            Object outcome = executeWithRetryAndTimeout(tool, args, context);
            results.put(tool.getName(), outcome);
        }
        return results;
    }

    public Map<String, Object> executeToolsParallel(List<Tool> tools, Map<String, Map<String, Object>> toolArgs, ToolExecutionContext context) {
        log.info("Executing tools in parallel: count={}", tools.size());
        Map<String, Object> results = new ConcurrentHashMap<>();
        ExecutorService executor = Executors.newFixedThreadPool(Math.min(tools.size(), 8));

        try {
            List<CompletableFuture<Void>> futures = tools.stream()
                    .map(tool -> CompletableFuture.runAsync(() -> {
                        Map<String, Object> args = toolArgs.getOrDefault(tool.getName(), new HashMap<>());
                        Object outcome = executeWithRetryAndTimeout(tool, args, context);
                        results.put(tool.getName(), outcome);
                    }, executor))
                    .toList();

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Parallel tools execution timed out or encountered an error", e);
        } finally {
            executor.shutdown();
        }

        return results;
    }

    public Object executeWithRetryAndTimeout(Tool tool, Map<String, Object> arguments, ToolExecutionContext context) {
        String correlationId = UUID.randomUUID().toString();
        eventPublisher.publishEvent(new ToolExecutionStartedEvent(context.getOrganizationId(), context.getConversationId(), tool.getName(), correlationId));

        long startTime = System.currentTimeMillis();
        int attempts = 3;
        int currentAttempt = 0;
        Object result = null;
        boolean success = false;
        Exception lastException = null;

        while (currentAttempt < attempts && !success) {
            currentAttempt++;
            try {
                result = executeFuture(tool, arguments, context);
                success = true;
            } catch (Exception e) {
                lastException = e;
                log.warn("Attempt {} failed for tool {}: {}", currentAttempt, tool.getName(), e.getMessage());
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        eventPublisher.publishEvent(new ToolExecutionCompletedEvent(context.getOrganizationId(), context.getConversationId(), tool.getName(), success, duration, correlationId));

        if (!success) {
            log.error("Tool {} failed after {} attempts.", tool.getName(), attempts);
            return "Execution failed: " + (lastException != null ? lastException.getMessage() : "unknown error");
        }

        return result;
    }

    private Object executeFuture(Tool tool, Map<String, Object> arguments, ToolExecutionContext context) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<Object> future = executor.submit(() -> tool.execute(arguments, context));
            return future.get(2, TimeUnit.SECONDS);
        } finally {
            executor.shutdown();
        }
    }
}
