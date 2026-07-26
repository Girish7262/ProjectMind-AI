package com.acciobuild.ai.tool.impl;

import com.acciobuild.ai.tool.Tool;
import com.acciobuild.ai.tool.ToolExecutionContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Tool for looking up context variables stored inside session memories.
 */
@Component
public class MemorySearchTool implements Tool {

    @Override
    public String getName() {
        return "Memory Search";
    }

    @Override
    public String getDescription() {
        return "Retrieves key-value memory variables for the active user context.";
    }

    @Override
    public Map<String, Object> getParameterSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("key", "string");
        return schema;
    }

    @Override
    public Object execute(Map<String, Object> arguments, ToolExecutionContext context) {
        String key = (String) arguments.getOrDefault("key", "");
        Map<String, Object> memoryMap = new HashMap<>();
        memoryMap.put("memoryKey", key);
        memoryMap.put("memoryValue", "[Memory State] Value for preference key " + key);
        return memoryMap;
    }
}
