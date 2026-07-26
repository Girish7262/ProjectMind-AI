package com.acciobuild.knowledge.event;

import com.acciobuild.knowledge.domain.event.KnowledgePublishedEvent;
import com.acciobuild.knowledge.domain.model.KnowledgeOutboxEvent;
import com.acciobuild.knowledge.domain.repository.KnowledgeOutboxRepository;
import com.acciobuild.knowledge.event.publisher.impl.ApplicationEventPublisherAdapter;
import com.acciobuild.knowledge.scheduler.OutboxEventProcessor;
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
 * Unit tests validating knowledge event serialization, outbox logging interceptor, and publishers.
 */
@ExtendWith(MockitoExtension.class)
public class OutboxEventTest {

    @Mock private KnowledgeOutboxRepository outboxRepository;
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
        UUID docId = UUID.randomUUID();
        KnowledgePublishedEvent event = new KnowledgePublishedEvent(orgId, docId, "corr-id-123");

        String json = serializer.serialize(event);
        assertNotNull(json);
        assertTrue(json.contains("KNOWLEDGE_PUBLISHED"));

        KnowledgePublishedEvent deserialized = serializer.deserialize(json, KnowledgePublishedEvent.class);
        assertEquals(docId, deserialized.getDocumentId());
        assertEquals(orgId, deserialized.getOrganizationId());
    }

    @Test
    void testOutboxLogging_Success() {
        UUID orgId = UUID.randomUUID();
        UUID docId = UUID.randomUUID();
        KnowledgePublishedEvent event = new KnowledgePublishedEvent(orgId, docId, "corr-id-123");

        adapter.handleDomainEvent(event);

        verify(outboxRepository).save(any(KnowledgeOutboxEvent.class));
    }

    @Test
    void testOutboxProcessor_Success() {
        KnowledgeOutboxEvent outbox = new KnowledgeOutboxEvent();
        outbox.setId(UUID.randomUUID());
        outbox.setAggregateId(UUID.randomUUID().toString());
        outbox.setAggregateType("KNOWLEDGE_DOCUMENT");
        outbox.setEventType("KNOWLEDGE_PUBLISHED");
        outbox.setPayloadJson("{}");
        outbox.setStatus("PENDING");

        when(outboxRepository.findByStatusOrderByCreatedAtAsc("PENDING"))
                .thenReturn(Collections.singletonList(outbox));

        processor.processOutboxEvents();

        assertEquals("PUBLISHED", outbox.getStatus());
        assertNotNull(outbox.getPublishedAt());
        verify(outboxRepository).save(outbox);
    }
}
