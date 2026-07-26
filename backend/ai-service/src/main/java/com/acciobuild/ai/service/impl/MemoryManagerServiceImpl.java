package com.acciobuild.ai.service.impl;

import com.acciobuild.ai.domain.event.*;
import com.acciobuild.ai.domain.model.AiConversation;
import com.acciobuild.ai.domain.model.AiConversationMemory;
import com.acciobuild.ai.domain.model.AiConversationMessage;
import com.acciobuild.ai.domain.repository.AiConversationMemoryRepository;
import com.acciobuild.ai.domain.repository.AiConversationRepository;
import com.acciobuild.ai.dto.MemoryDto;
import com.acciobuild.ai.enums.MemoryScope;
import com.acciobuild.ai.exception.ConversationNotFoundException;
import com.acciobuild.ai.exception.InvalidConversationStateException;
import com.acciobuild.ai.multitenancy.TenantContext;
import com.acciobuild.ai.memory.MemoryConflictResolver;
import com.acciobuild.ai.memory.MemoryConflictResolver.ConflictStrategy;
import com.acciobuild.ai.memory.MemorySummarizer;
import com.acciobuild.ai.service.MemoryManagerService;
import com.acciobuild.ai.service.MemoryRetentionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Service implementation orchestrating conversation memory actions and boundary restrictions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MemoryManagerServiceImpl implements MemoryManagerService {

    private final AiConversationMemoryRepository memoryRepository;
    private final AiConversationRepository conversationRepository;
    private final MemorySummarizer memorySummarizer;
    private final MemoryConflictResolver conflictResolver;
    private final MemoryRetentionService retentionService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    @CacheEvict(value = "memories", key = "#conversationId")
    public MemoryDto createMemory(UUID conversationId, MemoryDto dto) {
        log.info("Creating memory for conversation {} with key {}", conversationId, dto.getMemoryKey());

        AiConversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ConversationNotFoundException("Conversation not found to create memory: " + conversationId));

        validateOwnership(conv);

        if (dto.getMemoryScope() == null) {
            dto.setMemoryScope(MemoryScope.CONVERSATION);
        }

        // Verify uniqueness
        List<AiConversationMemory> existing = memoryRepository.findByConversationId(conversationId);
        boolean keyExists = existing.stream()
                .anyMatch(m -> m.getMemoryKey().equalsIgnoreCase(dto.getMemoryKey()) && m.getMemoryScope() == dto.getMemoryScope());

        if (keyExists) {
            throw new InvalidConversationStateException("Memory variable already exists for key: " + dto.getMemoryKey());
        }

        // Guard against circular references
        checkCircularReferences(conversationId, dto.getMemoryKey(), dto.getMemoryValue(), new LinkedHashSet<>());

        AiConversationMemory memory = new AiConversationMemory();
        memory.setId(UUID.randomUUID());
        memory.setOrganizationId(conv.getOrganizationId());
        memory.setConversationId(conversationId);
        memory.setMemoryScope(dto.getMemoryScope());
        memory.setMemoryKey(dto.getMemoryKey());
        memory.setMemoryValue(dto.getMemoryValue());
        memory.setCreatedAt(LocalDateTime.now());
        memory.setUpdatedAt(LocalDateTime.now());

        AiConversationMemory saved = memoryRepository.save(memory);

        eventPublisher.publishEvent(new MemoryCreatedEvent(
                saved.getOrganizationId(),
                saved.getId(),
                conversationId,
                saved.getMemoryScope().name(),
                saved.getMemoryKey(),
                UUID.randomUUID().toString()
        ));

        // Enforce retention rules
        retentionService.enforceRetentionForConversation(conversationId);

        return mapToDto(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = "memories", key = "#conversationId")
    public MemoryDto updateMemory(UUID conversationId, MemoryDto dto, ConflictStrategy strategy) {
        log.info("Updating memory for conversation {} with key {}", conversationId, dto.getMemoryKey());

        AiConversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ConversationNotFoundException("Conversation not found to update memory: " + conversationId));

        validateOwnership(conv);

        List<AiConversationMemory> existingList = memoryRepository.findByConversationId(conversationId);
        Optional<AiConversationMemory> existing = existingList.stream()
                .filter(m -> m.getMemoryKey().equalsIgnoreCase(dto.getMemoryKey()) && m.getMemoryScope() == dto.getMemoryScope())
                .findFirst();

        AiConversationMemory memory;
        if (existing.isPresent()) {
            memory = existing.get();
            String resolvedValue = conflictResolver.resolveConflict(memory.getMemoryValue(), dto.getMemoryValue(), strategy);
            
            // Check circular dependency on modified value
            checkCircularReferences(conversationId, memory.getMemoryKey(), resolvedValue, new LinkedHashSet<>());
            
            memory.setMemoryValue(resolvedValue);
            memory.setUpdatedAt(LocalDateTime.now());
        } else {
            // If doesn't exist, create it
            return createMemory(conversationId, dto);
        }

        AiConversationMemory saved = memoryRepository.save(memory);

        eventPublisher.publishEvent(new MemoryUpdatedEvent(
                saved.getOrganizationId(),
                conversationId,
                saved.getMemoryScope().name(),
                UUID.randomUUID().toString()
        ));

        return mapToDto(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = "memories", key = "#conversationId")
    public MemoryDto mergeMemories(UUID conversationId, String sourceKey, String targetKey, ConflictStrategy strategy) {
        log.info("Merging memories: {} into {} for conversation {}", sourceKey, targetKey, conversationId);

        AiConversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ConversationNotFoundException("Conversation not found: " + conversationId));
        validateOwnership(conv);

        List<AiConversationMemory> memories = memoryRepository.findByConversationId(conversationId);
        
        AiConversationMemory source = memories.stream()
                .filter(m -> m.getMemoryKey().equalsIgnoreCase(sourceKey))
                .findFirst()
                .orElseThrow(() -> new InvalidConversationStateException("Source memory key not found: " + sourceKey));

        AiConversationMemory target = memories.stream()
                .filter(m -> m.getMemoryKey().equalsIgnoreCase(targetKey))
                .findFirst()
                .orElseThrow(() -> new InvalidConversationStateException("Target memory key not found: " + targetKey));

        String mergedValue = conflictResolver.resolveConflict(target.getMemoryValue(), source.getMemoryValue(), strategy);
        
        checkCircularReferences(conversationId, target.getMemoryKey(), mergedValue, new LinkedHashSet<>());

        target.setMemoryValue(mergedValue);
        target.setUpdatedAt(LocalDateTime.now());
        memoryRepository.save(target);

        // Delete source memory
        memoryRepository.delete(source);

        eventPublisher.publishEvent(new MemoryMergedEvent(
                target.getOrganizationId(),
                conversationId,
                target.getMemoryScope().name(),
                sourceKey,
                targetKey,
                UUID.randomUUID().toString()
        ));

        return mapToDto(target);
    }

    @Override
    @Transactional
    @CacheEvict(value = "memories", key = "#conversationId")
    public List<MemoryDto> splitMemory(UUID conversationId, String key, String delimiter) {
        log.info("Splitting memory key {} for conversation {}", key, conversationId);

        AiConversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ConversationNotFoundException("Conversation not found: " + conversationId));
        validateOwnership(conv);

        AiConversationMemory original = memoryRepository.findByConversationId(conversationId).stream()
                .filter(m -> m.getMemoryKey().equalsIgnoreCase(key))
                .findFirst()
                .orElseThrow(() -> new InvalidConversationStateException("Memory key not found: " + key));

        String val = original.getMemoryValue();
        if (val == null) {
            val = "";
        }
        
        String[] parts = val.split(Pattern.quote(delimiter));
        List<MemoryDto> splitResults = new ArrayList<>();

        // Create new memory keys for each split part
        for (int i = 0; i < parts.length; i++) {
            String partVal = parts[i].strip();
            if (partVal.isEmpty()) {
                continue;
            }

            MemoryDto partDto = MemoryDto.builder()
                    .memoryScope(original.getMemoryScope())
                    .memoryKey(original.getMemoryKey() + "_part" + (i + 1))
                    .memoryValue(partVal)
                    .build();

            splitResults.add(createMemory(conversationId, partDto));
        }

        // Delete original memory key
        memoryRepository.delete(original);

        return splitResults;
    }

    @Override
    @Transactional
    @CacheEvict(value = "memories", key = "#conversationId")
    public void expireMemory(UUID conversationId, String key) {
        log.info("Expiring memory key {} for conversation {}", key, conversationId);

        AiConversationMemory memory = memoryRepository.findByConversationId(conversationId).stream()
                .filter(m -> m.getMemoryKey().equalsIgnoreCase(key))
                .findFirst()
                .orElseThrow(() -> new InvalidConversationStateException("Memory key not found: " + key));

        validateOwnership(conversationRepository.findById(conversationId).get());

        memoryRepository.delete(memory);

        eventPublisher.publishEvent(new MemoryExpiredEvent(
                memory.getOrganizationId(),
                conversationId,
                memory.getMemoryScope().name(),
                key,
                UUID.randomUUID().toString()
        ));
    }

    @Override
    @Transactional
    @CacheEvict(value = "memories", key = "#conversationId")
    public void archiveMemory(UUID conversationId, String key) {
        log.info("Archiving memory key {} for conversation {}", key, conversationId);

        AiConversationMemory memory = memoryRepository.findByConversationId(conversationId).stream()
                .filter(m -> m.getMemoryKey().equalsIgnoreCase(key))
                .findFirst()
                .orElseThrow(() -> new InvalidConversationStateException("Memory key not found: " + key));

        validateOwnership(conversationRepository.findById(conversationId).get());

        memory.setMemoryKey("archived:" + memory.getMemoryKey());
        memory.setUpdatedAt(LocalDateTime.now());
        AiConversationMemory saved = memoryRepository.save(memory);

        eventPublisher.publishEvent(new MemoryArchivedEvent(
                saved.getOrganizationId(),
                saved.getId(),
                conversationId,
                saved.getMemoryScope().name(),
                UUID.randomUUID().toString()
        ));
    }

    @Override
    @Transactional
    @CacheEvict(value = "memories", key = "#conversationId")
    public void deleteMemory(UUID conversationId, String key) {
        log.info("Deleting memory key {} for conversation {}", key, conversationId);

        AiConversationMemory memory = memoryRepository.findByConversationId(conversationId).stream()
                .filter(m -> m.getMemoryKey().equalsIgnoreCase(key))
                .findFirst()
                .orElseThrow(() -> new InvalidConversationStateException("Memory key not found: " + key));

        validateOwnership(conversationRepository.findById(conversationId).get());

        memoryRepository.delete(memory);
    }

    @Override
    public String summarizeMemory(UUID conversationId) {
        AiConversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ConversationNotFoundException("Conversation not found: " + conversationId));
        validateOwnership(conv);

        List<String> messagesContent = conv.getMessages().stream()
                .map(AiConversationMessage::getContent)
                .collect(Collectors.toList());

        return memorySummarizer.summarize(messagesContent);
    }

    @Override
    @Cacheable(value = "memories", key = "#conversationId")
    public List<MemoryDto> getMemories(UUID conversationId) {
        AiConversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ConversationNotFoundException("Conversation not found: " + conversationId));
        validateOwnership(conv);

        return memoryRepository.findByConversationId(conversationId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<MemoryDto> getMemoriesByScope(UUID conversationId, MemoryScope scope) {
        AiConversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ConversationNotFoundException("Conversation not found: " + conversationId));
        validateOwnership(conv);

        return memoryRepository.findByConversationId(conversationId).stream()
                .filter(m -> m.getMemoryScope() == scope)
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private void checkCircularReferences(UUID conversationId, String key, String value, Set<String> visiting) {
        if (value == null || !value.contains("{{")) {
            return;
        }
        visiting.add(key);
        Pattern pattern = Pattern.compile("\\{\\{([^}]+)\\}\\}");
        Matcher matcher = pattern.matcher(value);
        
        List<AiConversationMemory> memories = memoryRepository.findByConversationId(conversationId);
        
        while (matcher.find()) {
            String refKey = matcher.group(1).trim();
            if (visiting.contains(refKey)) {
                throw new IllegalArgumentException("Circular reference detected in memory values: " + String.join(" -> ", visiting) + " -> " + refKey);
            }
            
            memories.stream()
                .filter(m -> m.getMemoryKey().equalsIgnoreCase(refKey))
                .findFirst()
                .ifPresent(m -> checkCircularReferences(conversationId, refKey, m.getMemoryValue(), new LinkedHashSet<>(visiting)));
        }
    }

    private void validateOwnership(AiConversation conv) {
        UUID currentTenant = TenantContext.getCurrentTenant();
        if (currentTenant != null && !currentTenant.equals(conv.getOrganizationId())) {
            throw new SecurityException("Tenant isolation boundary violation: operation denied.");
        }
    }

    private MemoryDto mapToDto(AiConversationMemory m) {
        return MemoryDto.builder()
                .id(m.getId())
                .organizationId(m.getOrganizationId())
                .conversationId(m.getConversationId())
                .memoryScope(m.getMemoryScope())
                .memoryKey(m.getMemoryKey())
                .memoryValue(m.getMemoryValue())
                .createdAt(m.getCreatedAt())
                .updatedAt(m.getUpdatedAt())
                .build();
    }
}
