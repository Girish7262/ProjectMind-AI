package com.acciobuild.ai.provider;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying nested path resolution, token statistics mapping, and fallback logic in AiResponseParser.
 */
public class AiResponseParserTest {

    private final AiResponseParser parser = new AiResponseParser();

    @Test
    @SuppressWarnings("unchecked")
    void testParseStructuredMapSuccessfully() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("text", "Processed outcome");
        payload.put("finishReason", "stop");

        Map<String, Object> usage = new HashMap<>();
        usage.put("prompt_tokens", 100);
        usage.put("completion_tokens", 50);
        usage.put("cost", 0.003);
        payload.put("usage", usage);

        AiResponse parsed = parser.parseRawResponse(payload, "OpenAI", "gpt-4o", 230L);

        assertNotNull(parsed);
        assertEquals("Processed outcome", parsed.getGeneratedText());
        assertEquals("stop", parsed.getFinishReason());
        assertNotNull(parsed.getUsageStatistics());
        assertEquals(100, parsed.getUsageStatistics().getInputTokens());
        assertEquals(50, parsed.getUsageStatistics().getOutputTokens());
        assertEquals(0.003, parsed.getUsageStatistics().getEstimatedCost());
        assertEquals(230L, parsed.getLatencyMs());
    }

    @Test
    void testParseRawTextFallback() {
        String payload = "Simple textual string";
        AiResponse parsed = parser.parseRawResponse(payload, "Ollama", "llama3", 100L);

        assertNotNull(parsed);
        assertEquals("Simple textual string", parsed.getGeneratedText());
        assertEquals("stop", parsed.getFinishReason());
        assertNotNull(parsed.getUsageStatistics());
        assertEquals(15, parsed.getUsageStatistics().getInputTokens());
        assertEquals(35, parsed.getUsageStatistics().getOutputTokens());
    }
}
