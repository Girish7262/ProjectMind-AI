package com.acciobuild.ai.tool.impl;

import com.acciobuild.ai.tool.Tool;
import com.acciobuild.ai.tool.ToolExecutionContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Tool for looking up active organization configuration policies.
 */
@Component
public class OrganizationSearchTool implements Tool {

    @Override
    public String getName() {
        return "Organization Search";
    }

    @Override
    public String getDescription() {
        return "Looks up organization policies, thresholds, and configuration settings.";
    }

    @Override
    public Map<String, Object> getParameterSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("organizationId", "string");
        return schema;
    }

    @Override
    public Object execute(Map<String, Object> arguments, ToolExecutionContext context) {
        String orgId = (String) arguments.getOrDefault("organizationId", "");
        Map<String, Object> policy = new HashMap<>();
        policy.put("organizationId", orgId);
        policy.put("retentionPolicyDays", 90);
        policy.put("safetyStrictness", "HIGH");
        return policy;
    }
}
