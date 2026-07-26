package com.acciobuild.ai.domain.repository;

import com.acciobuild.ai.domain.model.AiConversationMemory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository for AiConversationMemory.
 */
@Repository
public interface AiConversationMemoryRepository extends JpaRepository<AiConversationMemory, UUID>, JpaSpecificationExecutor<AiConversationMemory> {
    
    List<AiConversationMemory> findByConversationId(UUID conversationId);

    Optional<com.acciobuild.ai.domain.projection.MemorySummary> findSummaryById(UUID id);

    List<com.acciobuild.ai.domain.projection.MemorySummary> findSummariesByConversationId(UUID conversationId);
}
