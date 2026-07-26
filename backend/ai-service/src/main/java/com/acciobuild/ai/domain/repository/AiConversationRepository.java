package com.acciobuild.ai.domain.repository;

import com.acciobuild.ai.domain.model.AiConversation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository for AiConversation.
 */
@Repository
public interface AiConversationRepository extends JpaRepository<AiConversation, UUID>, JpaSpecificationExecutor<AiConversation> {
    
    List<AiConversation> findByProjectId(UUID projectId);

    @EntityGraph(attributePaths = {"messages"})
    Optional<AiConversation> findWithMessagesById(UUID id);

    Optional<com.acciobuild.ai.domain.projection.ConversationSummary> findSummaryById(UUID id);

    List<com.acciobuild.ai.domain.projection.ConversationSummary> findSummariesByProjectId(UUID projectId);
}
