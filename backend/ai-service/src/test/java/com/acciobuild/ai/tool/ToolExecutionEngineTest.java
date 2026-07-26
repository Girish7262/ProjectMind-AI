package com.acciobuild.ai.tool;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests verifying retry limits, timeouts, and concurrent execution in ToolExecutionEngine.
 */
@ExtendWith(MockitoExtension.class)
public class ToolExecutionEngineTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ToolExecutionEngine engine;

    @Test
    void testExecutionRetryUponTransientErrors() {
        Tool failingTool = mock(Tool.class);
        when(failingTool.getName()).thenReturn("Mock Fails");
        when(failingTool.execute(any(), any())).thenThrow(new RuntimeException("Transient failure"));

        ToolExecutionContext context = ToolExecutionContext.builder()
                .conversationId(UUID.randomUUID())
                .organizationId(UUID.randomUUID())
                .traceId("trace-123")
                .build();

        Object outcome = engine.executeWithRetryAndTimeout(failingTool, new HashMap<>(), context);
        assertNotNull(outcome);
        assertTrue(outcome.toString().contains("Execution failed"));
        
        // Verifies 3 execution attempts occurred
        verify(failingTool, times(3)).execute(any(), any());
        verify(eventPublisher, times(2)).publishEvent(any(Object.class));
    }

    @Test
    void testParallelExecutionsCombined() {
        Tool t1 = mock(Tool.class);
        when(t1.getName()).thenReturn("Tool-1");
        when(t1.execute(any(), any())).thenReturn("Result-1");

        Tool t2 = mock(Tool.class);
        when(t2.getName()).thenReturn("Tool-2");
        when(t2.execute(any(), any())).thenReturn("Result-2");

        ToolExecutionContext context = ToolExecutionContext.builder()
                .conversationId(UUID.randomUUID())
                .organizationId(UUID.randomUUID())
                .traceId("trace-456")
                .build();

        Map<String, Object> outcomes = engine.executeToolsParallel(List.of(t1, t2), new HashMap<>(), context);
        assertEquals(2, outcomes.size());
        assertEquals("Result-1", outcomes.get("Tool-1"));
        assertEquals("Result-2", outcomes.get("Tool-2"));
    }
}
