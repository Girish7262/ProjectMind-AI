package com.acciobuild.ai.service;

import com.acciobuild.ai.domain.model.AiPromptVersion;
import com.acciobuild.ai.prompt.PromptComposer;
import com.acciobuild.ai.prompt.PromptVariableResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Unit tests verifying prompt variable resolution and block aggregation in PromptComposer.
 */
@ExtendWith(MockitoExtension.class)
public class PromptComposerTest {

    @Mock
    private PromptVariableResolver variableResolver;

    @InjectMocks
    private PromptComposer promptComposer;

    @Test
    void testComposeCombinesSectionsCorrectly() {
        AiPromptVersion version = new AiPromptVersion();
        version.setSystemInstruction("System: {{user}} instructions.");
        version.setUserTemplate("User: {{organization}} payload.");

        UUID convId = UUID.randomUUID();
        Map<String, Object> vars = new HashMap<>();

        when(variableResolver.resolve(eq("System: {{user}} instructions."), any(), eq(convId)))
                .thenReturn("System: admin instructions.");
        when(variableResolver.resolve(eq("User: {{organization}} payload."), any(), eq(convId)))
                .thenReturn("User: Acme Corp payload.");

        String composed = promptComposer.compose(
                version,
                vars,
                convId,
                "Dev instructions",
                "Execute tasks",
                "Be safe",
                "Citation info"
        );

        assertTrue(composed.contains("=== SAFETY POLICY ===\nBe safe"));
        assertTrue(composed.contains("=== DEVELOPER INSTRUCTIONS ===\nDev instructions"));
        assertTrue(composed.contains("=== SYSTEM INSTRUCTIONS ===\nSystem: admin instructions."));
        assertTrue(composed.contains("=== INSTRUCTIONS ===\nExecute tasks"));
        assertTrue(composed.contains("=== CITATIONS ===\nCitation info"));
        assertTrue(composed.contains("=== USER INPUT ===\nUser: Acme Corp payload."));
    }
}
