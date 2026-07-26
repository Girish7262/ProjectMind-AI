package com.acciobuild.ai.service.impl;

import com.acciobuild.ai.domain.event.CitationAttachedEvent;
import com.acciobuild.ai.domain.model.AiCitation;
import com.acciobuild.ai.domain.model.AiConversationMessage;
import com.acciobuild.ai.domain.repository.AiCitationRepository;
import com.acciobuild.ai.domain.repository.AiConversationMessageRepository;
import com.acciobuild.ai.dto.CitationDto;
import com.acciobuild.ai.exception.CitationValidationException;
import com.acciobuild.ai.service.CitationService;
import com.acciobuild.ai.multitenancy.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service Implementation for generated response reference citations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CitationServiceImpl implements CitationService {

    private final AiCitationRepository citationRepository;
    private final AiConversationMessageRepository messageRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public List<CitationDto> getCitationsForMessage(UUID messageId) {
        return citationRepository.findByMessageId(messageId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void recordCitations(UUID messageId, List<CitationDto> citations) {
        log.info("Recording citations for message ID: {}", messageId);

        AiConversationMessage msg = messageRepository.findById(messageId)
                .orElseThrow(() -> new CitationValidationException("Message not found to attach citations: " + messageId));

        List<AiCitation> existing = citationRepository.findByMessageId(messageId);
        Set<UUID> existingSourceIds = existing.stream()
                .map(AiCitation::getSourceId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        for (CitationDto dto : citations) {
            // Prevent duplicate citations with same source id for this message
            if (dto.getSourceId() != null && existingSourceIds.contains(dto.getSourceId())) {
                log.warn("Skipping duplicate citation source reference: {}", dto.getSourceId());
                continue;
            }

            AiCitation citation = new AiCitation();
            citation.setId(UUID.randomUUID());
            citation.setOrganizationId(TenantContext.getCurrentTenant() != null ? TenantContext.getCurrentTenant() : msg.getOrganizationId());
            citation.setMessage(msg);
            citation.setCitationType(dto.getCitationType());
            citation.setSourceId(dto.getSourceId());
            citation.setTitle(dto.getTitle());
            citation.setUrl(dto.getUrl());
            citation.setSnippet(dto.getSnippet());

            AiCitation saved = citationRepository.save(citation);

            eventPublisher.publishEvent(new CitationAttachedEvent(
                    saved.getOrganizationId(),
                    saved.getId(),
                    msg.getId(),
                    UUID.randomUUID().toString()
            ));

            if (dto.getSourceId() != null) {
                existingSourceIds.add(dto.getSourceId());
            }
        }
    }

    private CitationDto mapToDto(AiCitation citation) {
        return CitationDto.builder()
                .id(citation.getId())
                .citationType(citation.getCitationType())
                .sourceId(citation.getSourceId())
                .title(citation.getTitle())
                .url(citation.getUrl())
                .snippet(citation.getSnippet())
                .build();
    }
}
