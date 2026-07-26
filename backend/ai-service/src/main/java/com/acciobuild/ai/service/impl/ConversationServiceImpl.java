package com.acciobuild.ai.service.impl;

import com.acciobuild.ai.domain.event.ConversationArchivedEvent;
import com.acciobuild.ai.domain.event.ConversationCreatedEvent;
import com.acciobuild.ai.domain.event.ConversationDeletedEvent;
import com.acciobuild.ai.domain.model.AiConversation;
import com.acciobuild.ai.domain.repository.AiConversationRepository;
import com.acciobuild.ai.dto.ConversationDto;
import com.acciobuild.ai.dto.MessageDto;
import com.acciobuild.ai.enums.ConversationStatus;
import com.acciobuild.ai.exception.ConversationNotFoundException;
import com.acciobuild.ai.exception.InvalidConversationStateException;
import com.acciobuild.ai.service.ConversationService;
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
 * Service Implementation for AI Conversation management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ConversationServiceImpl implements ConversationService {

    private final AiConversationRepository conversationRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public ConversationDto createConversation(ConversationDto dto) {
        log.info("Creating new AI conversation for project: {}", dto.getProjectId());

        AiConversation conv = new AiConversation();
        conv.setId(UUID.randomUUID());
        conv.setOrganizationId(TenantContext.getCurrentTenant());
        conv.setProjectId(dto.getProjectId());
        conv.setTitle(dto.getTitle() != null ? dto.getTitle() : "New Conversation");
        conv.setStatus(ConversationStatus.ACTIVE);
        conv.setModelProvider(dto.getModelProvider());
        conv.setModelName(dto.getModelName());
        conv.setTemperature(dto.getTemperature());
        conv.setSystemInstruction(dto.getSystemInstruction());
        conv.setCreatedAt(LocalDateTime.now());
        conv.setUpdatedAt(LocalDateTime.now());

        AiConversation saved = conversationRepository.save(conv);

        eventPublisher.publishEvent(new ConversationCreatedEvent(
                saved.getOrganizationId(),
                saved.getId(),
                saved.getProjectId(),
                UUID.randomUUID().toString()
        ));

        return mapToDto(saved);
    }

    @Override
    public ConversationDto getConversation(UUID id) {
        AiConversation conv = conversationRepository.findById(id)
                .orElseThrow(() -> new ConversationNotFoundException("Conversation not found with ID: " + id));
        validateOwnership(conv);
        return mapToDto(conv);
    }

    @Override
    public List<ConversationDto> getConversationsByProject(UUID projectId) {
        return conversationRepository.findByProjectId(projectId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MessageDto addMessage(UUID conversationId, MessageDto messageDto) {
        // Handled in detail inside MessageService or direct delegate
        return null;
    }

    @Override
    @Transactional
    public void archiveConversation(UUID id) {
        log.info("Archiving conversation: {}", id);
        AiConversation conv = conversationRepository.findById(id)
                .orElseThrow(() -> new ConversationNotFoundException("Conversation not found with ID: " + id));
        validateOwnership(conv);

        conv.setStatus(ConversationStatus.ARCHIVED);
        conv.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conv);

        eventPublisher.publishEvent(new ConversationArchivedEvent(
                conv.getOrganizationId(),
                conv.getId(),
                UUID.randomUUID().toString()
        ));
    }

    @Override
    @Transactional
    public void renameConversation(UUID id, String newTitle) {
        AiConversation conv = conversationRepository.findById(id)
                .orElseThrow(() -> new ConversationNotFoundException("Conversation not found with ID: " + id));
        validateOwnership(conv);

        conv.setTitle(newTitle);
        conv.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conv);
    }

    @Override
    @Transactional
    public void softDeleteConversation(UUID id) {
        log.info("Soft deleting conversation: {}", id);
        AiConversation conv = conversationRepository.findById(id)
                .orElseThrow(() -> new ConversationNotFoundException("Conversation not found with ID: " + id));
        validateOwnership(conv);

        conv.setStatus(ConversationStatus.CLOSED);
        conv.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conv);

        eventPublisher.publishEvent(new ConversationDeletedEvent(
                conv.getOrganizationId(),
                conv.getId(),
                UUID.randomUUID().toString()
        ));
    }

    @Override
    @Transactional
    public void permanentDeleteConversation(UUID id) {
        log.info("Permanently deleting conversation: {}", id);
        AiConversation conv = conversationRepository.findById(id)
                .orElseThrow(() -> new ConversationNotFoundException("Conversation not found with ID: " + id));
        validateOwnership(conv);

        conversationRepository.delete(conv);
    }

    @Override
    @Transactional
    public void restoreConversation(UUID id) {
        log.info("Restoring conversation: {}", id);
        AiConversation conv = conversationRepository.findById(id)
                .orElseThrow(() -> new ConversationNotFoundException("Conversation not found with ID: " + id));
        validateOwnership(conv);

        conv.setStatus(ConversationStatus.ACTIVE);
        conv.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conv);
    }

    private void validateOwnership(AiConversation conv) {
        UUID currentTenant = TenantContext.getCurrentTenant();
        if (currentTenant != null && !currentTenant.equals(conv.getOrganizationId())) {
            throw new InvalidConversationStateException("Access denied to conversation representing another tenant.");
        }
    }

    private ConversationDto mapToDto(AiConversation conv) {
        return ConversationDto.builder()
                .id(conv.getId())
                .organizationId(conv.getOrganizationId())
                .projectId(conv.getProjectId())
                .title(conv.getTitle())
                .status(conv.getStatus())
                .modelProvider(conv.getModelProvider())
                .modelName(conv.getModelName())
                .temperature(conv.getTemperature())
                .systemInstruction(conv.getSystemInstruction())
                .createdAt(conv.getCreatedAt())
                .updatedAt(conv.getUpdatedAt())
                .build();
    }
}
