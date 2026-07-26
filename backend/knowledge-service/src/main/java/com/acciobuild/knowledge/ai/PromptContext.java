package com.acciobuild.knowledge.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.Map;

/**
 * Capture prompt templates, variable substitutions, and instructions passed to AI interfaces.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptContext implements Serializable {

    private static final long serialVersionUID = 1L;

    private String promptTemplate;
    private Map<String, Object> variables;
    private String systemInstructions;
}
