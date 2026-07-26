package com.acciobuild.ai.service;

import com.acciobuild.ai.client.KnowledgeServiceClient;
import com.acciobuild.ai.client.OrganizationServiceClient;
import com.acciobuild.ai.client.ProjectServiceClient;
import com.acciobuild.ai.domain.model.*;
import com.acciobuild.ai.domain.repository.*;
import com.acciobuild.ai.dto.*;
import com.acciobuild.ai.enums.*;
import com.acciobuild.ai.exception.*;
import com.acciobuild.ai.service.impl.*;
import com.acciobuild.ai.multitenancy.TenantContext;
import com.acciobuild.common.dto.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests verifying business layer constraints, exceptions, and event triggers.
 */
@ExtendWith(MockitoExtension.class)
public class AiServiceTest {

    @Mock
    private AiConversationRepository conversationRepository;
    @Mock
    private AiConversationMessageRepository messageRepository;
    @Mock
    private AiPromptTemplateRepository templateRepository;
    @Mock
    private AiPromptVersionRepository versionRepository;
    @Mock
    private AiContextRepository contextRepository;
    @Mock
    private AiConversationMemoryRepository memoryRepository;
    @Mock
    private ProjectServiceClient projectClient;
    @Mock
    private OrganizationServiceClient organizationClient;
    @Mock
    private KnowledgeServiceClient knowledgeClient;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ConversationServiceImpl conversationService;
    @InjectMocks
    private ConversationMessageServiceImpl messageService;
    @InjectMocks
    private PromptTemplateServiceImpl promptTemplateService;
    @InjectMocks
    private ContextBuilderServiceImpl contextBuilderService;
    @InjectMocks
    private MemoryServiceImpl memoryService;

    private UUID tenantId;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        TenantContext.setCurrentTenant(tenantId);
    }

    @Test
    void testCreateConversationPublishesEvent() {
        ConversationDto dto = ConversationDto.builder()
                .projectId(UUID.randomUUID())
                .title("New Conversation")
                .modelProvider(ProviderType.OPENAI)
                .modelName("gpt-4o")
                .temperature(0.7)
                .build();

        AiConversation mockConv = new AiConversation();
        mockConv.setId(UUID.randomUUID());
        mockConv.setOrganizationId(tenantId);
        mockConv.setProjectId(dto.getProjectId());
        mockConv.setTitle(dto.getTitle());
        mockConv.setStatus(ConversationStatus.ACTIVE);

        when(conversationRepository.save(any(AiConversation.class))).thenReturn(mockConv);

        ConversationDto result = conversationService.createConversation(dto);

        assertNotNull(result);
        assertEquals("New Conversation", result.getTitle());
        verify(eventPublisher, times(1)).publishEvent(any(Object.class));
    }

    @Test
    void testOwnershipValidationThrowsException() {
        UUID otherTenant = UUID.randomUUID();
        AiConversation conv = new AiConversation();
        conv.setId(UUID.randomUUID());
        conv.setOrganizationId(otherTenant);

        when(conversationRepository.findById(conv.getId())).thenReturn(Optional.of(conv));

        assertThrows(InvalidConversationStateException.class, () -> {
            conversationService.getConversation(conv.getId());
        });
    }

    @Test
    void testAddMessageThrowsOnInactiveConversation() {
        AiConversation conv = new AiConversation();
        conv.setId(UUID.randomUUID());
        conv.setStatus(ConversationStatus.ARCHIVED);

        when(conversationRepository.findById(conv.getId())).thenReturn(Optional.of(conv));

        MessageDto msgDto = MessageDto.builder()
                .role(MessageRole.USER)
                .content("Hello")
                .build();

        assertThrows(InvalidConversationStateException.class, () -> {
            messageService.addMessage(conv.getId(), msgDto);
        });
    }

    @Test
    void testPromptTemplateNameCollisionThrowsException() {
        PromptTemplateDto dto = PromptTemplateDto.builder()
                .name("Standard Assistant")
                .build();

        when(templateRepository.findByName("Standard Assistant")).thenReturn(Optional.of(new AiPromptTemplate()));

        assertThrows(DuplicatePromptTemplateException.class, () -> {
            promptTemplateService.createTemplate(dto);
        });
    }

    @Test
    void testBuildContextGathersAndRanksSuccessfully() {
        UUID conversationId = UUID.randomUUID();
        AiConversation conv = new AiConversation();
        conv.setId(conversationId);
        conv.setProjectId(UUID.randomUUID());
        conv.setOrganizationId(tenantId);

        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conv));

        // Mock external client calls
        ApiResponse<Object> projectRes = new ApiResponse<>(200, "Success", new Object());
        when(projectClient.getProjectById(any(UUID.class))).thenReturn(projectRes);

        ApiResponse<Object> orgRes = new ApiResponse<>(200, "Success", new Object());
        when(organizationClient.getOrganizationById(any(UUID.class))).thenReturn(orgRes);

        List<Object> mockSearchDocs = List.of("Doc Chunk 1", "Doc Chunk 2");
        ApiResponse<List<Object>> knowledgeRes = new ApiResponse<>(200, "Success", mockSearchDocs);
        when(knowledgeClient.searchDocuments(anyString(), any(UUID.class))).thenReturn(knowledgeRes);

        AiContext mockContext = new AiContext();
        mockContext.setId(UUID.randomUUID());
        mockContext.setConversationId(conversationId);
        mockContext.setQueryText("How does RAG work?");

        when(contextRepository.save(any(AiContext.class))).thenReturn(mockContext);

        ContextDto contextResult = contextBuilderService.buildContext(conversationId, "How does RAG work?");

        assertNotNull(contextResult);
        verify(eventPublisher, times(1)).publishEvent(any(Object.class));
    }

    @Test
    void testMemoryScopeValidation() {
        UUID conversationId = UUID.randomUUID();
        MemoryDto dto = MemoryDto.builder()
                .memoryKey("user_preferences")
                .memoryValue("dark_mode")
                .memoryScope(null) // null scope should violate rule
                .build();

        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(new AiConversation()));

        assertThrows(MemoryScopeException.class, () -> {
            memoryService.updateMemory(conversationId, dto);
        });
    }
}
