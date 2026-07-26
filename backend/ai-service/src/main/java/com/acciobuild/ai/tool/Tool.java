package com.acciobuild.ai.tool;

import java.util.Map;

/**
 * Interface representing a tool executable by the orchestration engine.
 */
public interface Tool {
    
    /**
     * Gets the unique name identifying this tool.
     */
    String getName();
    
    /**
     * Gets the description detailing tool utility.
     */
    String getDescription();
    
    /**
     * Returns parameter constraint schema definitions for validation.
     */
    Map<String, Object> getParameterSchema();
    
    /**
     * Executes the tool functionality within the trace boundary.
     */
    Object execute(Map<String, Object> arguments, ToolExecutionContext context);
}
