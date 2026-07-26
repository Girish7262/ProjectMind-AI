package com.acciobuild.ai.tool.impl;

import com.acciobuild.ai.tool.Tool;
import com.acciobuild.ai.tool.ToolExecutionContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tool for matching generated outputs with their original document citation links.
 */
@Component
public class CitationResolverTool implements Tool {

    @Override
    public String getName() {
        return "Citation Resolver";
    }

    @Override
    public String getDescription() {
        return "Matches and resolves indices representing original source documents.";
    }

    @Override
    public Map<String, Object> getParameterSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("sourceIds", "list");
        return schema;
    }

    @Override
    public Object execute(Map<String, Object> arguments, ToolExecutionContext context) {
        return List.of("[Citation Resolved] Reference-1 matches SourceDoc-A");
    }
}
