package com.acciobuild.ai.tool;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Selector performing natural keyword token scans on query text to filter matching tools.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ToolSelector {

    private final ToolRegistry registry;

    public List<Tool> selectTools(String queryText) {
        log.info("Auto-selecting tools matching query: '{}'", queryText);
        if (queryText == null || queryText.strip().isEmpty()) {
            return Collections.emptyList();
        }

        List<Tool> selected = new ArrayList<>();
        String normalized = queryText.toLowerCase();

        if (normalized.contains("search") || normalized.contains("find") || normalized.contains("knowledge")) {
            addIfPresent(selected, "Knowledge Search");
        }
        if (normalized.contains("project") || normalized.contains("config")) {
            addIfPresent(selected, "Project Search");
        }
        if (normalized.contains("org") || normalized.contains("policy")) {
            addIfPresent(selected, "Organization Search");
        }
        if (normalized.contains("chat") || normalized.contains("history") || normalized.contains("conversation")) {
            addIfPresent(selected, "Conversation Search");
        }
        if (normalized.contains("memory") || normalized.contains("pref")) {
            addIfPresent(selected, "Memory Search");
        }
        if (normalized.contains("compile") || normalized.contains("prompt")) {
            addIfPresent(selected, "Prompt Resolver");
        }
        if (normalized.contains("context")) {
            addIfPresent(selected, "Context Builder");
        }
        if (normalized.contains("cite") || normalized.contains("source")) {
            addIfPresent(selected, "Citation Resolver");
        }

        if (selected.isEmpty()) {
            addIfPresent(selected, "Knowledge Search");
        }

        return selected;
    }

    private void addIfPresent(List<Tool> list, String name) {
        Tool tool = registry.getTool(name);
        if (tool != null && !list.contains(tool)) {
            list.add(tool);
        }
    }
}
