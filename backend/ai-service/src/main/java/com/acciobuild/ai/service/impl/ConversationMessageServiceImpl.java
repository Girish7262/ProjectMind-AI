package com.acciobuild.ai.service.impl;

import com.acciobuild.ai.domain.event.MessageAddedEvent;
import com.acciobuild.ai.domain.model.AiConversation;
import com.acciobuild.ai.domain.model.AiConversationMessage;
import com.acciobuild.ai.domain.repository.AiConversationMessageRepository;
import com.acciobuild.ai.domain.repository.AiConversationRepository;
import com.acciobuild.ai.dto.MessageDto;
import com.acciobuild.ai.enums.ConversationStatus;
import com.acciobuild.ai.exception.ConversationNotFoundException;
import com.acciobuild.ai.exception.InvalidConversationStateException;
import com.acciobuild.ai.service.ConversationMessageService;
import com.acciobuild.ai.multitenancy.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service Implementation managing messages in AI conversations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ConversationMessageServiceImpl implements ConversationMessageService {

    private final AiConversationRepository conversationRepository;
    private final AiConversationMessageRepository messageRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public MessageDto addMessage(UUID conversationId, MessageDto dto) {
        log.info("Adding message to conversation: {}", conversationId);

        AiConversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ConversationNotFoundException("Conversation not found with ID: " + conversationId));

        if (conv.getStatus() != ConversationStatus.ACTIVE) {
            throw new InvalidConversationStateException("Cannot add messages to a non-active conversation.");
        }

        AiConversationMessage msg = new AiConversationMessage();
        msg.setId(UUID.randomUUID());
        msg.setOrganizationId(TenantContext.getCurrentTenant() != null ? TenantContext.getCurrentTenant() : conv.getOrganizationId());
        msg.setConversation(conv);
        msg.setRole(dto.getRole());
        msg.setContent(dto.getContent());
        msg.setPromptTokens(dto.getPromptTokens() != null ? dto.getPromptTokens() : 0);
        msg.setCompletionTokens(dto.getCompletionTokens() != null ? dto.getCompletionTokens() : 0);
        msg.setTotalTokens(msg.getPromptTokens() + msg.getCompletionTokens());
        msg.setResponseDurationMs(dto.getResponseDurationMs());
        msg.setCreatedAt(LocalDateTime.now());

        AiConversationMessage saved = messageRepository.save(msg);

        eventPublisher.publishEvent(new MessageAddedEvent(
                saved.getOrganizationId(),
                conv.getId(),
                saved.getId(),
                saved.getRole().name(),
                UUID.randomUUID().toString()
        ));

        return mapToDto(saved);
    }

    @Override
    public List<MessageDto> getMessagesByConversation(UUID conversationId) {
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteMessage(UUID messageId) {
        log.info("Deleting message ID: {}", messageId);
        AiConversationMessage msg = messageRepository.findById(messageId)
                .orElseThrow(() -> new InvalidConversationStateException("Message not found with ID: " + messageId));
        messageRepository.delete(msg);
    }

    private MessageDto mapToDto(AiConversationMessage msg) {
        return MessageDto.builder()
                .id(msg.getId())
                .role(msg.getRole())
                .content(msg.getContent())
                .promptTokens(msg.getPromptTokens())
                .completionTokens(msg.getCompletionTokens())
                .totalTokens(msg.getTotalTokens())
                .responseDurationMs(msg.getResponseDurationMs())
                .createdAt(msg.getCreatedAt())
                .build();
    }
}
