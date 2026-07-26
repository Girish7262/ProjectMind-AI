package com.acciobuild.ai.service;

import com.acciobuild.ai.domain.model.AiConversationMemory;
import com.acciobuild.ai.domain.repository.AiConversationMemoryRepository;
import com.acciobuild.ai.enums.MemoryScope;
import com.acciobuild.ai.service.impl.MemoryRetentionServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

/**
 * Unit tests verifying that memory retention checks prune excess records correctly.
 */
@ExtendWith(MockitoExtension.class)
public class MemoryRetentionTest {

    @Mock
    private AiConversationMemoryRepository memoryRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private MemoryRetentionServiceImpl memoryRetentionService;

    @Test
    void testEnforceRetentionForConversationPrunesExcess() {
        UUID conversationId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();

        List<AiConversationMemory> memories = new ArrayList<>();
        // Create 22 memory keys (max is 20)
        for (int i = 0; i < 22; i++) {
            AiConversationMemory memory = new AiConversationMemory();
            memory.setId(UUID.randomUUID());
            memory.setOrganizationId(tenantId);
            memory.setConversationId(conversationId);
            memory.setMemoryKey("key" + i);
            memory.setMemoryScope(MemoryScope.CONVERSATION);
            memory.setUpdatedAt(LocalDateTime.now().minusMinutes(i));
            memories.add(memory);
        }

        when(memoryRepository.findByConversationId(conversationId)).thenReturn(memories);

        memoryRetentionService.enforceRetentionForConversation(conversationId);

        verify(memoryRepository, times(2)).delete(any(AiConversationMemory.class));
        verify(eventPublisher, times(2)).publishEvent(any(Object.class));
    }
}
