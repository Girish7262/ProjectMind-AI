package com.acciobuild.ai.tool.impl;

import com.acciobuild.ai.tool.Tool;
import com.acciobuild.ai.tool.ToolExecutionContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tool for dynamically building active context models.
 */
@Component
public class ContextBuilderTool implements Tool {

    @Override
    public String getName() {
        return "Context Builder";
    }

    @Override
    public String getDescription() {
        return "Assembles dynamic conversation context lists based on search history and priorities.";
    }

    @Override
    public Map<String, Object> getParameterSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("conversationId", "string");
        return schema;
    }

    @Override
    public Object execute(Map<String, Object> arguments, ToolExecutionContext context) {
        return List.of("[Built Context 1]", "[Built Context 2]");
    }
}
