package com.acciobuild.project.event;

import com.acciobuild.project.domain.event.ProjectCreatedEvent;
import com.acciobuild.project.domain.model.OutboxEvent;
import com.acciobuild.project.domain.repository.OutboxEventRepository;
import com.acciobuild.project.event.publisher.impl.ApplicationEventPublisherAdapter;
import com.acciobuild.project.scheduler.OutboxEventProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import java.util.Collections;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests validating event serialization, outbox persistence interceptor,
 * and Kafka retry/publishing simulations.
 */
@ExtendWith(MockitoExtension.class)
public class OutboxEventTest {

    @Mock private OutboxEventRepository outboxRepository;
    @Mock private ApplicationEventPublisher applicationEventPublisher;
    @InjectMocks private EventSerializer serializer;

    private ApplicationEventPublisherAdapter adapter;
    private OutboxEventProcessor processor;

    @BeforeEach
    void setUp() {
        adapter = new ApplicationEventPublisherAdapter(applicationEventPublisher, outboxRepository, serializer);
        processor = new OutboxEventProcessor(outboxRepository);
    }

    @Test
    void testSerialization_Success() {
        UUID orgId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        ProjectCreatedEvent event = new ProjectCreatedEvent(orgId, projectId, "ml-engine", "corr-id-123");

        String json = serializer.serialize(event);
        assertNotNull(json);
        assertTrue(json.contains("PROJECT_CREATED"));

        ProjectCreatedEvent deserialized = serializer.deserialize(json, ProjectCreatedEvent.class);
        assertEquals("ml-engine", deserialized.getProjectCode());
        assertEquals(orgId, deserialized.getOrganizationId());
    }

    @Test
    void testOutboxLogging_Success() {
        UUID orgId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        ProjectCreatedEvent event = new ProjectCreatedEvent(orgId, projectId, "ml-engine", "corr-id-123");

        adapter.handleDomainEvent(event);

        verify(outboxRepository).save(any(OutboxEvent.class));
    }

    @Test
    void testOutboxProcessor_Success() {
        OutboxEvent outbox = new OutboxEvent();
        outbox.setEventId(UUID.randomUUID());
        outbox.setAggregateId("aggregate-1");
        outbox.setAggregateType("PROJECT");
        outbox.setEventType("PROJECT_CREATED");
        outbox.setPayload("{}");
        outbox.setStatus("PENDING");

        when(outboxRepository.findByStatusOrderByCreatedAtAsc("PENDING"))
                .thenReturn(Collections.singletonList(outbox));

        processor.processOutboxEvents();

        assertEquals("PUBLISHED", outbox.getStatus());
        assertNotNull(outbox.getPublishedAt());
        verify(outboxRepository).save(outbox);
    }
}
