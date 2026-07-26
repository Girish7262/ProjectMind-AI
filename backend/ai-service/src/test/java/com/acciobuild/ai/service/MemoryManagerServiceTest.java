package com.acciobuild.ai.service;

import com.acciobuild.ai.domain.model.AiConversation;
import com.acciobuild.ai.domain.model.AiConversationMemory;
import com.acciobuild.ai.domain.repository.AiConversationMemoryRepository;
import com.acciobuild.ai.domain.repository.AiConversationRepository;
import com.acciobuild.ai.dto.MemoryDto;
import com.acciobuild.ai.enums.MemoryScope;
import com.acciobuild.ai.exception.InvalidConversationStateException;
import com.acciobuild.ai.memory.MemoryConflictResolver;
import com.acciobuild.ai.memory.MemoryConflictResolver.ConflictStrategy;
import com.acciobuild.ai.memory.MemorySummarizer;
import com.acciobuild.ai.multitenancy.TenantContext;
import com.acciobuild.ai.service.impl.MemoryManagerServiceImpl;
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
 * Unit tests verifying creation, circular checks, merging, and splitting of memories.
 */
@ExtendWith(MockitoExtension.class)
public class MemoryManagerServiceTest {

    @Mock
    private AiConversationMemoryRepository memoryRepository;
    @Mock
    private AiConversationRepository conversationRepository;
    @Mock
    private MemorySummarizer memorySummarizer;
    @Mock
    private MemoryConflictResolver conflictResolver;
    @Mock
    private MemoryRetentionService retentionService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private MemoryManagerServiceImpl memoryManagerService;

    private UUID tenantId;
    private UUID conversationId;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        conversationId = UUID.randomUUID();
        TenantContext.setCurrentTenant(tenantId);
    }

    @Test
    void testCreateMemorySuccessfully() {
        AiConversation conv = new AiConversation();
        conv.setId(conversationId);
        conv.setOrganizationId(tenantId);

        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conv));
        when(memoryRepository.findByConversationId(conversationId)).thenReturn(Collections.emptyList());

        MemoryDto dto = MemoryDto.builder()
                .memoryKey("user_pref")
                .memoryValue("verbose")
                .memoryScope(MemoryScope.CONVERSATION)
                .build();

        when(memoryRepository.save(any(AiConversationMemory.class))).thenAnswer(inv -> inv.getArgument(0));

        MemoryDto result = memoryManagerService.createMemory(conversationId, dto);

        assertNotNull(result);
        assertEquals("user_pref", result.getMemoryKey());
        verify(eventPublisher, times(1)).publishEvent(any(Object.class));
        verify(retentionService, times(1)).enforceRetentionForConversation(conversationId);
    }

    @Test
    void testCreateMemoryFailsOnCircularReference() {
        AiConversation conv = new AiConversation();
        conv.setId(conversationId);
        conv.setOrganizationId(tenantId);

        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conv));

        AiConversationMemory cyclicTarget = new AiConversationMemory();
        cyclicTarget.setMemoryKey("key1");
        cyclicTarget.setMemoryValue("Values: {{key2}}");
        cyclicTarget.setMemoryScope(MemoryScope.CONVERSATION);

        when(memoryRepository.findByConversationId(conversationId)).thenReturn(List.of(cyclicTarget));

        MemoryDto dto = MemoryDto.builder()
                .memoryKey("key2")
                .memoryValue("Values: {{key1}}")
                .memoryScope(MemoryScope.CONVERSATION)
                .build();

        assertThrows(IllegalArgumentException.class, () -> {
            memoryManagerService.createMemory(conversationId, dto);
        });
    }

    @Test
    void testMergeMemoriesIntegratesKeys() {
        AiConversation conv = new AiConversation();
        conv.setId(conversationId);
        conv.setOrganizationId(tenantId);

        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conv));

        AiConversationMemory source = new AiConversationMemory();
        source.setMemoryKey("sourceKey");
        source.setMemoryValue("Value A");
        source.setMemoryScope(MemoryScope.CONVERSATION);

        AiConversationMemory target = new AiConversationMemory();
        target.setMemoryKey("targetKey");
        target.setMemoryValue("Value B");
        target.setMemoryScope(MemoryScope.CONVERSATION);

        when(memoryRepository.findByConversationId(conversationId)).thenReturn(List.of(source, target));
        when(conflictResolver.resolveConflict("Value B", "Value A", ConflictStrategy.APPEND)).thenReturn("Value B | Value A");

        MemoryDto result = memoryManagerService.mergeMemories(conversationId, "sourceKey", "targetKey", ConflictStrategy.APPEND);

        assertNotNull(result);
        assertEquals("Value B | Value A", result.getMemoryValue());
        verify(memoryRepository, times(1)).delete(source);
    }
}
