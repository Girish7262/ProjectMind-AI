package com.acciobuild.ai.provider;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying constraints, range validation, and parameters mapping in AiRequestBuilder.
 */
public class AiRequestBuilderTest {

    @Test
    void testBuildSuccessfully() {
        AiRequest request = new AiRequestBuilder()
                .prompt("Hello world")
                .temperature(0.8)
                .maxTokens(150)
                .build();

        assertNotNull(request);
        assertEquals("Hello world", request.getPrompt());
        assertEquals(0.8, request.getTemperature());
        assertEquals(150, request.getMaxTokens());
    }

    @Test
    void testBuildThrowsOnEmptyPrompt() {
        assertThrows(IllegalArgumentException.class, () -> {
            new AiRequestBuilder().prompt("").build();
        });
    }

    @Test
    void testBuildThrowsOnInvalidTemperature() {
        assertThrows(IllegalArgumentException.class, () -> {
            new AiRequestBuilder().prompt("Help").temperature(2.5).build();
        });
    }

    @Test
    void testBuildThrowsOnInvalidTopP() {
        assertThrows(IllegalArgumentException.class, () -> {
            new AiRequestBuilder().prompt("Help").topP(-0.5).build();
        });
    }

    @Test
    void testBuildThrowsOnInvalidMaxTokens() {
        assertThrows(IllegalArgumentException.class, () -> {
            new AiRequestBuilder().prompt("Help").maxTokens(0).build();
        });
    }
}
