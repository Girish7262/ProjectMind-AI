package com.acciobuild.project.event.publisher.impl;

import com.acciobuild.project.domain.event.ProjectDomainEvent;
import com.acciobuild.project.domain.model.OutboxEvent;
import com.acciobuild.project.domain.repository.OutboxEventRepository;
import com.acciobuild.project.event.EventSerializer;
import com.acciobuild.project.event.publisher.ProjectEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event publisher adapter implementing ProjectEventPublisher.
 * Translates domain events into spring application events, catches them,
 * and records them to the outbox database logs table.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ApplicationEventPublisherAdapter implements ProjectEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final OutboxEventRepository outboxRepository;
    private final EventSerializer serializer;

    @Override
    public void publish(ProjectDomainEvent event) {
        log.debug("Publishing spring application event: {}", event.getEventType());
        applicationEventPublisher.publishEvent(event);
    }

    /**
     * Listens to all project service domain events statelessly and records them in the outbox.
     */
    @EventListener
    public void handleDomainEvent(ProjectDomainEvent event) {
        log.info("Intercepted domain event: {}. Writing to Outbox...", event.getEventType());

        OutboxEvent outbox = new OutboxEvent();
        outbox.setEventId(UUID.randomUUID());
        outbox.setAggregateType("PROJECT");
        outbox.setAggregateId(event.getOrganizationId().toString());
        outbox.setEventType(event.getEventType());
        outbox.setPayload(serializer.serialize(event));
        outbox.setStatus("PENDING");
        outbox.setCreatedAt(LocalDateTime.now());
        outbox.setCorrelationId(event.getCorrelationId());

        outboxRepository.save(outbox);
        log.info("Outbox log entry recorded: {}", outbox.getEventId());
    }
}
