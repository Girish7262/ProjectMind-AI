package com.acciobuild.ai.tool.impl;

import com.acciobuild.ai.tool.Tool;
import com.acciobuild.ai.tool.ToolExecutionContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Tool for performing conversational history lookups.
 */
@Component
public class ConversationSearchTool implements Tool {

    @Override
    public String getName() {
        return "Conversation Search";
    }

    @Override
    public String getDescription() {
        return "Searches messages and active headers within conversation history boundaries.";
    }

    @Override
    public Map<String, Object> getParameterSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("conversationId", "string");
        return schema;
    }

    @Override
    public Object execute(Map<String, Object> arguments, ToolExecutionContext context) {
        String convId = (String) arguments.getOrDefault("conversationId", "");
        Map<String, Object> result = new HashMap<>();
        result.put("conversationId", convId);
        result.put("historyLength", 12);
        result.put("lastActiveAt", "2026-07-25T21:00:00Z");
        return result;
    }
}
