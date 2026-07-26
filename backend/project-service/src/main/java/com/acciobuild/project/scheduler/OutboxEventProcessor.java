package com.acciobuild.project.scheduler;

import com.acciobuild.project.domain.model.OutboxEvent;
import com.acciobuild.project.domain.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Outbox Event processor scanning for unsent logs and simulating forwarding them to Kafka.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxEventProcessor {

    private final OutboxEventRepository outboxRepository;

    /**
     * Scans for PENDING outbox events every 5 seconds and simulates forwarding them
     * to a remote message broker (e.g., Apache Kafka).
     */
    @Scheduled(fixedDelay = 5000)
    @Transactional(rollbackFor = Exception.class)
    public void processOutboxEvents() {
        List<OutboxEvent> pending = outboxRepository.findByStatusOrderByCreatedAtAsc("PENDING");

        if (pending.isEmpty()) {
            return;
        }

        log.info("Processing {} pending Outbox events...", pending.size());

        for (OutboxEvent event : pending) {
            try {
                // Simulate publishing payload to Kafka broker
                log.info("Simulating publishing event ID: {} [Type: {}] to Kafka topic...",
                        event.getEventId(), event.getEventType());

                event.setStatus("PUBLISHED");
                event.setPublishedAt(LocalDateTime.now());
                outboxRepository.save(event);
                
                log.info("Successfully published event ID: {}", event.getEventId());
            } catch (Exception e) {
                log.error("Failed to publish event ID: {}. Scheduling retry...", event.getEventId(), e);
                event.setRetryCount(event.getRetryCount() + 1);
                if (event.getRetryCount() >= 5) {
                    event.setStatus("FAILED");
                }
                outboxRepository.save(event);
            }
        }
    }
}
