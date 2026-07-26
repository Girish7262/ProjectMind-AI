package com.acciobuild.ai.prompt;

import com.acciobuild.ai.domain.model.AiPromptVersion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * Composer facilitating aggregation and template compilation of system, developer, user,
 * context, memory, citation, instruction, and safety rules into a final consolidated block.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PromptComposer {

    private final PromptVariableResolver variableResolver;

    public String compose(
            AiPromptVersion version,
            Map<String, Object> variables,
            UUID conversationId,
            String developerPrompt,
            String instructionPrompt,
            String safetyPrompt,
            String citationPrompt) {

        if (version == null) {
            throw new IllegalArgumentException("Prompt version is required for composition.");
        }

        StringBuilder finalPrompt = new StringBuilder();

        // 1. Safety Prompt
        if (safetyPrompt != null && !safetyPrompt.strip().isEmpty()) {
            finalPrompt.append("=== SAFETY POLICY ===\n")
                       .append(safetyPrompt.strip())
                       .append("\n\n");
        }

        // 2. Developer / System Instruction Prompt
        String baseSystem = version.getSystemInstruction() != null ? version.getSystemInstruction() : "";
        String resolvedSystem = variableResolver.resolve(baseSystem, variables, conversationId);
        
        if (developerPrompt != null && !developerPrompt.strip().isEmpty()) {
            finalPrompt.append("=== DEVELOPER INSTRUCTIONS ===\n")
                       .append(developerPrompt.strip())
                       .append("\n\n");
        }
        
        if (!resolvedSystem.strip().isEmpty()) {
            finalPrompt.append("=== SYSTEM INSTRUCTIONS ===\n")
                       .append(resolvedSystem.strip())
                       .append("\n\n");
        }

        // 3. Instruction Override Prompts
        if (instructionPrompt != null && !instructionPrompt.strip().isEmpty()) {
            finalPrompt.append("=== INSTRUCTIONS ===\n")
                       .append(instructionPrompt.strip())
                       .append("\n\n");
        }

        // 4. Citations
        if (citationPrompt != null && !citationPrompt.strip().isEmpty()) {
            finalPrompt.append("=== CITATIONS ===\n")
                       .append(citationPrompt.strip())
                       .append("\n\n");
        }

        // 5. User Template
        String baseUser = version.getUserTemplate() != null ? version.getUserTemplate() : "";
        String resolvedUser = variableResolver.resolve(baseUser, variables, conversationId);
        if (!resolvedUser.strip().isEmpty()) {
            finalPrompt.append("=== USER INPUT ===\n")
                       .append(resolvedUser.strip())
                       .append("\n");
        }

        return finalPrompt.toString().trim();
    }
}
