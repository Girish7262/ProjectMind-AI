package com.acciobuild.ai.service.impl;

import com.acciobuild.ai.domain.event.MemoryUpdatedEvent;
import com.acciobuild.ai.domain.model.AiConversation;
import com.acciobuild.ai.domain.model.AiConversationMemory;
import com.acciobuild.ai.domain.repository.AiConversationMemoryRepository;
import com.acciobuild.ai.domain.repository.AiConversationRepository;
import com.acciobuild.ai.dto.MemoryDto;
import com.acciobuild.ai.exception.ConversationNotFoundException;
import com.acciobuild.ai.exception.MemoryScopeException;
import com.acciobuild.ai.service.MemoryService;
import com.acciobuild.ai.multitenancy.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service Implementation for AI Conversation Memory management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MemoryServiceImpl implements MemoryService {

    private final AiConversationMemoryRepository memoryRepository;
    private final AiConversationRepository conversationRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public List<MemoryDto> getMemoryForConversation(UUID conversationId) {
        return memoryRepository.findByConversationId(conversationId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateMemory(UUID conversationId, MemoryDto dto) {
        log.info("Updating memory for conversation: {}", conversationId);

        AiConversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ConversationNotFoundException("Conversation not found to update memory: " + conversationId));

        if (dto.getMemoryScope() == null) {
            throw new MemoryScopeException("Memory scope constraint violation: Scope cannot be null.");
        }

        List<AiConversationMemory> existingList = memoryRepository.findByConversationId(conversationId);
        Optional<AiConversationMemory> existing = existingList.stream()
                .filter(m -> m.getMemoryKey().equalsIgnoreCase(dto.getMemoryKey()) && m.getMemoryScope() == dto.getMemoryScope())
                .findFirst();

        AiConversationMemory memory;
        if (existing.isPresent()) {
            memory = existing.get();
            memory.setMemoryValue(dto.getMemoryValue());
            memory.setUpdatedAt(LocalDateTime.now());
        } else {
            memory = new AiConversationMemory();
            memory.setId(UUID.randomUUID());
            memory.setOrganizationId(TenantContext.getCurrentTenant() != null ? TenantContext.getCurrentTenant() : conv.getOrganizationId());
            memory.setConversationId(conversationId);
            memory.setMemoryScope(dto.getMemoryScope());
            memory.setMemoryKey(dto.getMemoryKey());
            memory.setMemoryValue(dto.getMemoryValue());
            memory.setCreatedAt(LocalDateTime.now());
            memory.setUpdatedAt(LocalDateTime.now());
        }

        AiConversationMemory saved = memoryRepository.save(memory);

        eventPublisher.publishEvent(new MemoryUpdatedEvent(
                saved.getOrganizationId(),
                conversationId,
                saved.getMemoryScope().name(),
                UUID.randomUUID().toString()
        ));
    }

    @Override
    @Transactional
    public void clearMemory(UUID conversationId) {
        log.info("Clearing memory variables for conversation: {}", conversationId);
        List<AiConversationMemory> memories = memoryRepository.findByConversationId(conversationId);
        memoryRepository.deleteAll(memories);
    }

    private MemoryDto mapToDto(AiConversationMemory m) {
        return MemoryDto.builder()
                .id(m.getId())
                .memoryScope(m.getMemoryScope())
                .memoryKey(m.getMemoryKey())
                .memoryValue(m.getMemoryValue())
                .createdAt(m.getCreatedAt())
                .updatedAt(m.getUpdatedAt())
                .build();
    }
}
