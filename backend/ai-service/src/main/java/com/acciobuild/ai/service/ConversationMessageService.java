package com.acciobuild.ai.service;

import com.acciobuild.ai.dto.MessageDto;
import java.util.List;
import java.util.UUID;

/**
 * Service Contract for AI Message operations.
 */
public interface ConversationMessageService {
    MessageDto addMessage(UUID conversationId, MessageDto messageDto);
    List<MessageDto> getMessagesByConversation(UUID conversationId);
    void deleteMessage(UUID messageId);
}
