package com.acciobuild.project.domain.repository;

import com.acciobuild.project.domain.model.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data Repository for OutboxEvent.
 */
@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    /**
     * Retrieves outbox events in PENDING or FAILED status ordered by creation timestamp.
     */
    List<OutboxEvent> findByStatusOrderByCreatedAtAsc(String status);
}
