package com.acciobuild.ai.tool;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Repository holding active tool interfaces, schema details, and lookups.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ToolRegistry {

    private final List<Tool> springTools;
    private final Map<String, Tool> tools = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        log.info("Initializing ToolRegistry with available Spring tool beans");
        for (Tool t : springTools) {
            registerTool(t);
        }
    }

    public void registerTool(Tool tool) {
        log.info("Registering tool: {}", tool.getName());
        tools.put(tool.getName().toLowerCase(), tool);
    }

    public Tool getTool(String name) {
        if (name == null) {
            return null;
        }
        return tools.get(name.toLowerCase());
    }

    public List<Tool> getAllTools() {
        return new ArrayList<>(tools.values());
    }
}
