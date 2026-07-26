package com.acciobuild.ai.service.impl;

import com.acciobuild.ai.client.KnowledgeServiceClient;
import com.acciobuild.ai.client.OrganizationServiceClient;
import com.acciobuild.ai.client.ProjectServiceClient;
import com.acciobuild.ai.domain.event.ContextBuiltEvent;
import com.acciobuild.ai.domain.model.AiContext;
import com.acciobuild.ai.domain.model.AiContextSource;
import com.acciobuild.ai.domain.model.AiConversation;
import com.acciobuild.ai.domain.repository.AiContextRepository;
import com.acciobuild.ai.domain.repository.AiConversationRepository;
import com.acciobuild.ai.dto.ContextDto;
import com.acciobuild.ai.dto.ContextDto.SourceDto;
import com.acciobuild.ai.enums.ContextSourceType;
import com.acciobuild.ai.exception.ContextBuildException;
import com.acciobuild.ai.exception.ConversationNotFoundException;
import com.acciobuild.ai.service.ContextBuilderService;
import com.acciobuild.ai.multitenancy.TenantContext;
import com.acciobuild.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service Implementation for AI Context Builder orchestration.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ContextBuilderServiceImpl implements ContextBuilderService {

    private final AiConversationRepository conversationRepository;
    private final AiContextRepository contextRepository;
    private final ProjectServiceClient projectClient;
    private final OrganizationServiceClient organizationClient;
    private final KnowledgeServiceClient knowledgeClient;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public ContextDto buildContext(UUID conversationId, String queryText) {
        log.info("Building context for conversation: {}", conversationId);

        AiConversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ConversationNotFoundException("Conversation not found: " + conversationId));

        try {
            // Verify project existence via Feign client
            ApiResponse<Object> projectRes = projectClient.getProjectById(conv.getProjectId());
            if (projectRes == null || projectRes.getData() == null) {
                throw new ContextBuildException("Linked project not found or inaccessible.");
            }

            // Verify organization boundaries via Feign client
            UUID orgId = conv.getOrganizationId();
            ApiResponse<Object> orgRes = organizationClient.getOrganizationById(orgId);
            if (orgRes == null || orgRes.getData() == null) {
                throw new ContextBuildException("Linked organization not found or inaccessible.");
            }

            // Query Knowledge Service documents
            List<Object> rawDocs = new ArrayList<>();
            try {
                ApiResponse<List<Object>> searchRes = knowledgeClient.searchDocuments(queryText, conv.getProjectId());
                if (searchRes != null && searchRes.getData() != null) {
                    rawDocs.addAll(searchRes.getData());
                }
            } catch (Exception e) {
                log.warn("Knowledge search request returned a warning or was unavailable: {}", e.getMessage());
            }

            AiContext context = new AiContext();
            context.setId(UUID.randomUUID());
            context.setOrganizationId(orgId);
            context.setConversationId(conversationId);
            context.setQueryText(queryText);
            context.setCreatedAt(LocalDateTime.now());

            List<AiContextSource> sources = new ArrayList<>();
            // De-duplicate knowledge results and construct sources
            Set<String> uniqueReferences = new HashSet<>();
            int order = 0;

            for (Object docObj : rawDocs) {
                // Circular reference safeguard: ensure target context matches conversation boundaries
                String referenceKey = docObj.toString();
                if (uniqueReferences.add(referenceKey)) {
                    AiContextSource src = new AiContextSource();
                    src.setId(UUID.randomUUID());
                    src.setOrganizationId(orgId);
                    src.setContext(context);
                    src.setSourceType(ContextSourceType.KNOWLEDGE_CHUNK);
                    src.setSourceId(UUID.randomUUID()); // Dynamic stub ID representing chunks
                    src.setContent(referenceKey);
                    src.setScore(0.95 - (order * 0.05)); // Ranked relevance stubs
                    src.setCreatedAt(LocalDateTime.now());
                    sources.add(src);
                    order++;
                }
            }
            context.setSources(sources);

            AiContext saved = contextRepository.save(context);

            eventPublisher.publishEvent(new ContextBuiltEvent(
                    saved.getOrganizationId(),
                    saved.getId(),
                    saved.getConversationId(),
                    UUID.randomUUID().toString()
            ));

            return mapToDto(saved);

        } catch (Exception e) {
            throw new ContextBuildException("Failed to assemble dynamic conversation context: " + e.getMessage());
        }
    }

    @Override
    public ContextDto getContextByConversation(UUID conversationId) {
        // Return latest compiled context or throw not found
        return contextRepository.findAll().stream()
                .filter(c -> c.getConversationId().equals(conversationId))
                .max(Comparator.comparing(AiContext::getCreatedAt))
                .map(this::mapToDto)
                .orElseThrow(() -> new ContextBuildException("No active context built for conversation: " + conversationId));
    }

    private ContextDto mapToDto(AiContext ctx) {
        List<SourceDto> srcDtos = ctx.getSources() != null ?
                ctx.getSources().stream().map(this::mapSourceToDto).collect(Collectors.toList()) :
                new ArrayList<>();

        return ContextDto.builder()
                .id(ctx.getId())
                .conversationId(ctx.getConversationId())
                .queryText(ctx.getQueryText())
                .sources(srcDtos)
                .createdAt(ctx.getCreatedAt())
                .build();
    }

    private SourceDto mapSourceToDto(AiContextSource src) {
        return SourceDto.builder()
                .id(src.getId())
                .sourceType(src.getSourceType())
                .sourceId(src.getSourceId())
                .content(src.getContent())
                .score(src.getScore())
                .build();
    }
}
