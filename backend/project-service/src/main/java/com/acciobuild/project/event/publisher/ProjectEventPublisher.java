package com.acciobuild.project.event.publisher;

import com.acciobuild.project.domain.event.ProjectDomainEvent;

/**
 * Interface contract to publish domain events.
 * Decouples business logic from transport details (Kafka/RabbitMQ/Spring Events).
 */
public interface ProjectEventPublisher {

    /**
     * Publishes a domain event.
     */
    void publish(ProjectDomainEvent event);
}
