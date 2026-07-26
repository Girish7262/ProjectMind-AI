package com.acciobuild.ai.tool.impl;

import com.acciobuild.ai.tool.Tool;
import com.acciobuild.ai.tool.ToolExecutionContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Tool for parsing and compiling template prompts dynamically.
 */
@Component
public class PromptResolverTool implements Tool {

    @Override
    public String getName() {
        return "Prompt Resolver";
    }

    @Override
    public String getDescription() {
        return "Compiles templates and replaces parameters dynamically with current session values.";
    }

    @Override
    public Map<String, Object> getParameterSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("template", "string");
        return schema;
    }

    @Override
    public Object execute(Map<String, Object> arguments, ToolExecutionContext context) {
        String template = (String) arguments.getOrDefault("template", "");
        return "[Resolved Prompt] Compiled prompt based on template: " + template;
    }
}
