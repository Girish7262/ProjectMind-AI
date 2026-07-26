package com.acciobuild.ai.tool.impl;

import com.acciobuild.ai.tool.Tool;
import com.acciobuild.ai.tool.ToolExecutionContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Tool for looking up active project metadata configuration schemas.
 */
@Component
public class ProjectSearchTool implements Tool {

    @Override
    public String getName() {
        return "Project Search";
    }

    @Override
    public String getDescription() {
        return "Searches project settings and configuration descriptions matching user parameters.";
    }

    @Override
    public Map<String, Object> getParameterSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("projectId", "string");
        return schema;
    }

    @Override
    public Object execute(Map<String, Object> arguments, ToolExecutionContext context) {
        String projectId = (String) arguments.getOrDefault("projectId", "");
        Map<String, Object> details = new HashMap<>();
        details.put("projectId", projectId);
        details.put("projectName", "AccioBuild Internal Project Core");
        details.put("status", "ACTIVE");
        details.put("priorityBoost", true);
        return details;
    }
}
