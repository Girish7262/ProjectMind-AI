package com.acciobuild.ai.engine;

import com.acciobuild.ai.domain.event.ConversationContextBuiltEvent;
import com.acciobuild.ai.domain.event.ConversationTokenEstimatedEvent;
import com.acciobuild.ai.domain.event.ConversationWindowUpdatedEvent;
import com.acciobuild.ai.domain.model.AiContext;
import com.acciobuild.ai.domain.model.AiContextSource;
import com.acciobuild.ai.domain.model.AiConversation;
import com.acciobuild.ai.domain.model.AiConversationMessage;
import com.acciobuild.ai.domain.repository.AiContextRepository;
import com.acciobuild.ai.domain.repository.AiConversationMessageRepository;
import com.acciobuild.ai.domain.repository.AiConversationRepository;
import com.acciobuild.ai.dto.ContextDto;
import com.acciobuild.ai.dto.ContextDto.SourceDto;
import com.acciobuild.ai.exception.ConversationNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service Implementation orchestrating RAG pipeline filters and publishing telemetry metrics.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ConversationEngineServiceImpl implements ConversationEngineService {

    private final AiConversationRepository conversationRepository;
    private final AiConversationMessageRepository messageRepository;
    private final AiContextRepository contextRepository;
    
    private final ConversationContextValidator contextValidator;
    private final ConversationContextAssembler contextAssembler;
    private final ConversationContextRanker contextRanker;
    private final ConversationWindowManager windowManager;
    private final ConversationTokenEstimator tokenEstimator;
    
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    @Cacheable(value = "conversationContexts", key = "#conversationId", unless = "#result == null")
    public ContextDto assembleConversationContext(UUID conversationId, String queryText) {
        log.info("Starting conversation context build pipeline for: {}", conversationId);

        AiConversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ConversationNotFoundException("Conversation not found with ID: " + conversationId));

        // 1. Validation
        contextValidator.validate(conv);

        // 2. Sliding window evaluation
        List<AiConversationMessage> fullHistory = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
        List<AiConversationMessage> activeWindow = windowManager.getWindow(fullHistory, 4000); // 4k token limit
        
        eventPublisher.publishEvent(new ConversationWindowUpdatedEvent(
                conv.getOrganizationId(),
                conversationId,
                activeWindow.size(),
                UUID.randomUUID().toString()
        ));

        // 3. Assemble and rank context sources
        List<SourceDto> rawSources = contextAssembler.assemble(
                conversationId, 
                conv.getProjectId(), 
                conv.getOrganizationId(), 
                queryText
        );
        List<SourceDto> rankedSources = contextRanker.rank(rawSources);

        // 4. Persistence
        AiContext contextEntity = new AiContext();
        contextEntity.setId(UUID.randomUUID());
        contextEntity.setOrganizationId(conv.getOrganizationId());
        contextEntity.setConversationId(conversationId);
        contextEntity.setQueryText(queryText);
        contextEntity.setCreatedAt(LocalDateTime.now());

        List<AiContextSource> mappedSources = new ArrayList<>();
        for (SourceDto s : rankedSources) {
            AiContextSource src = new AiContextSource();
            src.setId(UUID.randomUUID());
            src.setOrganizationId(conv.getOrganizationId());
            src.setContext(contextEntity);
            src.setSourceType(s.getSourceType());
            src.setSourceId(s.getSourceId());
            src.setContent(s.getContent());
            src.setScore(s.getScore());
            src.setCreatedAt(LocalDateTime.now());
            mappedSources.add(src);
        }
        contextEntity.setSources(mappedSources);
        contextRepository.save(contextEntity);

        // 5. Estimate tokens
        int estimatedHistoryTokens = activeWindow.stream()
                .mapToInt(m -> tokenEstimator.estimateTokens(m.getContent()))
                .sum();
        int estimatedContextTokens = rankedSources.stream()
                .mapToInt(s -> tokenEstimator.estimateTokens(s.getContent()))
                .sum();
        int totalEstimated = estimatedHistoryTokens + estimatedContextTokens;

        eventPublisher.publishEvent(new ConversationTokenEstimatedEvent(
                conv.getOrganizationId(),
                conversationId,
                totalEstimated,
                UUID.randomUUID().toString()
        ));

        eventPublisher.publishEvent(new ConversationContextBuiltEvent(
                conv.getOrganizationId(),
                conversationId,
                rankedSources.size(),
                UUID.randomUUID().toString()
        ));

        return ContextDto.builder()
                .id(contextEntity.getId())
                .conversationId(conversationId)
                .queryText(queryText)
                .sources(rankedSources)
                .createdAt(contextEntity.getCreatedAt())
                .build();
    }
}
