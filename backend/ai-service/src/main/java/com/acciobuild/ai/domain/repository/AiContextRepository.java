package com.acciobuild.ai.domain.repository;

import com.acciobuild.ai.domain.model.AiContext;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository for AiContext.
 */
@Repository
public interface AiContextRepository extends JpaRepository<AiContext, UUID>, JpaSpecificationExecutor<AiContext> {
    
    List<AiContext> findByConversationId(UUID conversationId);

    @EntityGraph(attributePaths = {"sources"})
    Optional<AiContext> findWithSourcesById(UUID id);

    Optional<com.acciobuild.ai.domain.projection.ContextSummary> findSummaryById(UUID id);
}
