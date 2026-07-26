package com.acciobuild.ai.service;

import com.acciobuild.ai.dto.MemoryDto;
import java.util.List;
import java.util.UUID;

/**
 * Service Contract for AI conversation summary memory store and state updates.
 */
public interface MemoryService {
    List<MemoryDto> getMemoryForConversation(UUID conversationId);
    void updateMemory(UUID conversationId, MemoryDto memoryDto);
    void clearMemory(UUID conversationId);
}
