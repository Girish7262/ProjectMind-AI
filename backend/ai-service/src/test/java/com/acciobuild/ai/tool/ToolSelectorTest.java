package com.acciobuild.ai.tool;

import com.acciobuild.ai.tool.impl.KnowledgeSearchTool;
import com.acciobuild.ai.tool.impl.ProjectSearchTool;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying natural language keyword selections in ToolSelector.
 */
@ExtendWith(MockitoExtension.class)
public class ToolSelectorTest {

    @Test
    void testKeywordSelections() {
        Tool kSearch = new KnowledgeSearchTool();
        Tool pSearch = new ProjectSearchTool();
        ToolRegistry registry = new ToolRegistry(List.of(kSearch, pSearch));
        registry.init();

        ToolSelector selector = new ToolSelector(registry);

        List<Tool> resolved = selector.selectTools("Please search the knowledge files and check project settings.");
        assertTrue(resolved.contains(kSearch));
        assertTrue(resolved.contains(pSearch));
    }
}
