package com.acciobuild.ai.tool;

import com.acciobuild.ai.tool.impl.KnowledgeSearchTool;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying registrations and parameters lookup inside the ToolRegistry.
 */
@ExtendWith(MockitoExtension.class)
public class ToolRegistryTest {

    @Test
    void testRegistryDiscoveryAndRetrieval() {
        Tool kSearch = new KnowledgeSearchTool();
        ToolRegistry registry = new ToolRegistry(List.of(kSearch));
        registry.init();

        assertEquals(1, registry.getAllTools().size());
        assertEquals(kSearch, registry.getTool("Knowledge Search"));
        assertNull(registry.getTool("Unknown Tool"));
    }
}
