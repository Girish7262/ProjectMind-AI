package com.acciobuild.knowledge.event.publisher;

import com.acciobuild.knowledge.domain.event.KnowledgeDomainEvent;

/**
 * Interface contract to publish knowledge events.
 * Decouples business layer from transport adapters (such as Apache Kafka).
 */
public interface KnowledgeEventPublisher {

    /**
     * Publishes a domain event.
     */
    void publish(KnowledgeDomainEvent event);
}
