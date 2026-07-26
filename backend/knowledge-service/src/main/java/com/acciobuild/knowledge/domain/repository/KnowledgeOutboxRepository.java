package com.acciobuild.knowledge.domain.repository;

import com.acciobuild.knowledge.domain.model.KnowledgeOutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data Repository for KnowledgeOutboxEvent.
 */
@Repository
public interface KnowledgeOutboxRepository extends JpaRepository<KnowledgeOutboxEvent, UUID> {

    /**
     * Lists outbox events matching status ordered by created timestamp.
     */
    List<KnowledgeOutboxEvent> findByStatusOrderByCreatedAtAsc(String status);

    /**
     * Counts outbox events by status.
     */
    long countByStatus(String status);
}
