package com.acciobuild.ai.service;

import com.acciobuild.ai.domain.model.AiConversation;
import com.acciobuild.ai.domain.repository.AiConversationRepository;
import com.acciobuild.ai.dto.ContextDto;
import com.acciobuild.ai.enums.ConversationStatus;
import com.acciobuild.ai.enums.ProviderType;
import com.acciobuild.ai.multitenancy.TenantContext;
import com.acciobuild.ai.orchestrator.ExecutionResult;
import com.acciobuild.ai.provider.AiProvider;
import com.acciobuild.ai.provider.AiProviderRegistry;
import com.acciobuild.ai.service.impl.RagOrchestratorServiceImpl;
import com.acciobuild.ai.tool.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests verifying active conversation resolution, tenant isolation, and composite ranking logic in RagOrchestrator.
 */
@ExtendWith(MockitoExtension.class)
public class RagOrchestratorServiceTest {

    @Mock
    private AiConversationRepository conversationRepository;
    @Mock
    private ContextBuilderService contextBuilderService;
    @Mock
    private MemoryManagerService memoryManagerService;
    @Mock
    private ToolRegistry toolRegistry;
    @Mock
    private ToolSelector toolSelector;
    @Mock
    private ToolExecutionEngine toolExecutionEngine;
    @Mock
    private AiProviderRegistry providerRegistry;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private RagOrchestratorServiceImpl orchestratorService;

    private UUID tenantId;
    private UUID conversationId;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        conversationId = UUID.randomUUID();
        TenantContext.setCurrentTenant(tenantId);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @SuppressWarnings("unchecked")
    void testSuccessfulOrchestrationPipeline() {
        AiConversation conversation = new AiConversation();
        conversation.setId(conversationId);
        conversation.setOrganizationId(tenantId);
        conversation.setStatus(ConversationStatus.ACTIVE);
        conversation.setProjectId(UUID.randomUUID());
        conversation.setTitle("Active Chat");

        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(contextBuilderService.buildContext(any(), any())).thenReturn(ContextDto.builder().build());
        when(memoryManagerService.getMemories(any())).thenReturn(Collections.emptyList());

        Tool mockTool = mock(Tool.class);
        when(mockTool.getName()).thenReturn("Knowledge Search");
        when(toolSelector.selectTools(any())).thenReturn(List.of(mockTool));

        Map<String, Object> chunk1 = new HashMap<>();
        chunk1.put("content", "Regular content");
        chunk1.put("score", 0.8);
        chunk1.put("pinned", false);

        Map<String, Object> chunk2 = new HashMap<>();
        chunk2.put("content", "Pinned context item");
        chunk2.put("score", 0.75);
        chunk2.put("pinned", true);

        Map<String, Object> toolResults = Map.of("Knowledge Search", List.of(chunk1, chunk2));
        when(toolExecutionEngine.executeToolsParallel(any(), any(), any())).thenReturn(toolResults);

        AiProvider mockProvider = mock(AiProvider.class);
        when(mockProvider.getType()).thenReturn(ProviderType.OPENAI);
        when(mockProvider.getDiscoverableModels()).thenReturn(List.of("gpt-4o"));
        when(providerRegistry.getActiveProviders()).thenReturn(List.of(mockProvider));

        ExecutionResult result = orchestratorService.orchestrate(conversationId, "Please search our files", new HashMap<>());

        assertNotNull(result);
        assertTrue(result.isSuccessful());
        assertNotNull(result.getPlan());
        assertEquals(ProviderType.OPENAI, result.getPlan().getSelectedProvider());

        List<Map<String, Object>> ranked = result.getPlan().getRetrievedKnowledge();
        assertEquals(2, ranked.size());
        assertEquals("Pinned context item", ranked.get(0).get("content"));

        verify(eventPublisher, atLeastOnce()).publishEvent(any(Object.class));
    }

    @Test
    void testTenantSecurityViolation() {
        AiConversation conversation = new AiConversation();
        conversation.setId(conversationId);
        conversation.setOrganizationId(UUID.randomUUID());
        conversation.setStatus(ConversationStatus.ACTIVE);

        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));

        ExecutionResult result = orchestratorService.orchestrate(conversationId, "Search query", new HashMap<>());
        assertFalse(result.isSuccessful());
        assertTrue(result.getErrorMessage().contains("Tenant isolation"));
    }
}
