package com.acciobuild.ai.service;

import com.acciobuild.ai.dto.ConversationDto;
import com.acciobuild.ai.dto.MessageDto;
import java.util.List;
import java.util.UUID;

/**
 * Service Contract for AI Conversation orchestrations and session updates.
 */
public interface ConversationService {
    ConversationDto createConversation(ConversationDto dto);
    ConversationDto getConversation(UUID id);
    List<ConversationDto> getConversationsByProject(UUID projectId);
    MessageDto addMessage(UUID conversationId, MessageDto messageDto);
    void archiveConversation(UUID id);
    void renameConversation(UUID id, String newTitle);
    void softDeleteConversation(UUID id);
    void permanentDeleteConversation(UUID id);
    void restoreConversation(UUID id);
}
