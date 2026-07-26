package com.acciobuild.ai.engine;

import com.acciobuild.ai.domain.model.AiConversation;
import com.acciobuild.ai.domain.model.AiConversationMessage;
import com.acciobuild.ai.domain.repository.AiContextRepository;
import com.acciobuild.ai.domain.repository.AiConversationMessageRepository;
import com.acciobuild.ai.domain.repository.AiConversationRepository;
import com.acciobuild.ai.dto.ContextDto;
import com.acciobuild.ai.dto.ContextDto.SourceDto;
import com.acciobuild.ai.enums.ContextSourceType;
import com.acciobuild.ai.enums.MessageRole;
import com.acciobuild.ai.multitenancy.TenantContext;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit test suite verifying context building pipeline stages, sliding windows, and ranking heuristics.
 */
@ExtendWith(MockitoExtension.class)
public class ConversationEngineTest {

    @Mock
    private AiConversationRepository conversationRepository;
    @Mock
    private AiConversationMessageRepository messageRepository;
    @Mock
    private AiContextRepository contextRepository;
    @Mock
    private ConversationContextValidator contextValidator;
    @Mock
    private ConversationContextAssembler contextAssembler;
    @Mock
    private ConversationContextRanker contextRanker;
    @Mock
    private ConversationWindowManager windowManager;
    @Mock
    private ConversationTokenEstimator tokenEstimator;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ConversationEngineServiceImpl conversationEngineService;

    private UUID tenantId;
    private UUID conversationId;
    private AiConversation conversation;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        conversationId = UUID.randomUUID();
        TenantContext.setCurrentTenant(tenantId);

        conversation = new AiConversation();
        conversation.setId(conversationId);
        conversation.setOrganizationId(tenantId);
        conversation.setProjectId(UUID.randomUUID());
    }

    @Test
    void testAssembleContext_Success() {
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));

        List<AiConversationMessage> mockHistory = new ArrayList<>();
        AiConversationMessage m = new AiConversationMessage();
        m.setRole(MessageRole.USER);
        m.setContent("Query content text");
        mockHistory.add(m);

        when(messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId)).thenReturn(mockHistory);
        when(windowManager.getWindow(eq(mockHistory), anyInt())).thenReturn(mockHistory);

        List<SourceDto> mockSources = List.of(
                SourceDto.builder()
                        .sourceType(ContextSourceType.KNOWLEDGE_CHUNK)
                        .content("Context chunk data details")
                        .score(0.95)
                        .build()
        );
        when(contextAssembler.assemble(eq(conversationId), any(), any(), anyString())).thenReturn(mockSources);
        when(contextRanker.rank(any())).thenReturn(mockSources);
        when(tokenEstimator.estimateTokens(anyString())).thenReturn(5);

        ContextDto result = conversationEngineService.assembleConversationContext(conversationId, "Query content text");

        assertNotNull(result);
        assertEquals(1, result.getSources().size());
        assertEquals("Context chunk data details", result.getSources().get(0).getContent());
        verify(eventPublisher, times(3)).publishEvent(any(Object.class));
    }
}
