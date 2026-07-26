package com.acciobuild.knowledge.event.publisher.impl;

import com.acciobuild.common.util.MdcHelper;
import com.acciobuild.knowledge.domain.event.KnowledgeDomainEvent;
import com.acciobuild.knowledge.domain.model.KnowledgeOutboxEvent;
import com.acciobuild.knowledge.domain.repository.KnowledgeOutboxRepository;
import com.acciobuild.knowledge.event.EventSerializer;
import com.acciobuild.knowledge.event.publisher.KnowledgeEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event publisher adapter implementing KnowledgeEventPublisher.
 * Translates domain events into spring application events, catches them,
 * and records them to the outbox database logs table.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ApplicationEventPublisherAdapter implements KnowledgeEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final KnowledgeOutboxRepository outboxRepository;
    private final EventSerializer serializer;

    @Override
    public void publish(KnowledgeDomainEvent event) {
        log.debug("Publishing spring application event: {}", event.getEventType());
        applicationEventPublisher.publishEvent(event);
    }

    /**
     * Intercepts all knowledge service events statelessly and records them in the outbox.
     */
    @EventListener
    public void handleDomainEvent(KnowledgeDomainEvent event) {
        log.info("Intercepted domain event: {}. Writing to Outbox...", event.getEventType());

        KnowledgeOutboxEvent outbox = new KnowledgeOutboxEvent();
        outbox.setId(UUID.randomUUID());
        outbox.setAggregateId(event.getOrganizationId().toString());
        outbox.setAggregateType("KNOWLEDGE_DOCUMENT");
        outbox.setEventType(event.getEventType());
        outbox.setEventVersion(1);
        outbox.setTenantId(event.getOrganizationId());
        outbox.setPayloadJson(serializer.serialize(event));
        outbox.setStatus("PENDING");
        outbox.setCreatedAt(LocalDateTime.now());
        outbox.setUpdatedAt(LocalDateTime.now());
        outbox.setCorrelationId(event.getCorrelationId());

        outboxRepository.save(outbox);
        log.info("Outbox log entry recorded: {}", outbox.getId());
    }
}
