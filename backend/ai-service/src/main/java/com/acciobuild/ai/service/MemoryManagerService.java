package com.acciobuild.ai.service;

import com.acciobuild.ai.dto.MemoryDto;
import com.acciobuild.ai.enums.MemoryScope;
import com.acciobuild.ai.memory.MemoryConflictResolver.ConflictStrategy;

import java.util.List;
import java.util.UUID;

/**
 * Service contract orchestrating conversational and organizational AI memory variables.
 */
public interface MemoryManagerService {
    MemoryDto createMemory(UUID conversationId, MemoryDto dto);
    MemoryDto updateMemory(UUID conversationId, MemoryDto dto, ConflictStrategy strategy);
    MemoryDto mergeMemories(UUID conversationId, String sourceKey, String targetKey, ConflictStrategy strategy);
    List<MemoryDto> splitMemory(UUID conversationId, String key, String delimiter);
    void expireMemory(UUID conversationId, String key);
    void archiveMemory(UUID conversationId, String key);
    void deleteMemory(UUID conversationId, String key);
    String summarizeMemory(UUID conversationId);
    List<MemoryDto> getMemories(UUID conversationId);
    List<MemoryDto> getMemoriesByScope(UUID conversationId, MemoryScope scope);
}
