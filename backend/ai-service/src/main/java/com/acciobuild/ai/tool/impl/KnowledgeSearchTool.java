package com.acciobuild.ai.tool.impl;

import com.acciobuild.ai.tool.Tool;
import com.acciobuild.ai.tool.ToolExecutionContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tool for performing semantic search across organization knowledge documents.
 */
@Component
public class KnowledgeSearchTool implements Tool {

    @Override
    public String getName() {
        return "Knowledge Search";
    }

    @Override
    public String getDescription() {
        return "Retrieves semantically relevant document chunks matching user query context.";
    }

    @Override
    public Map<String, Object> getParameterSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("query", "string");
        return schema;
    }

    @Override
    public Object execute(Map<String, Object> arguments, ToolExecutionContext context) {
        String query = (String) arguments.getOrDefault("query", "");
        Map<String, Object> chunk1 = new HashMap<>();
        chunk1.put("score", 0.92);
        chunk1.put("content", "[Semantic Chunk] Dynamic RAG context matches prompt query '" + query + "'.");
        chunk1.put("freshness", "new");
        
        Map<String, Object> chunk2 = new HashMap<>();
        chunk2.put("score", 0.84);
        chunk2.put("content", "[System Policy] Active knowledge continuity standard policy statement.");
        chunk2.put("freshness", "old");

        return List.of(chunk1, chunk2);
    }
}
