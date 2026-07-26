package com.acciobuild.ai.domain.repository;

import com.acciobuild.ai.domain.model.AiConversationMessage;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository for AiConversationMessage.
 */
@Repository
public interface AiConversationMessageRepository extends JpaRepository<AiConversationMessage, UUID>, JpaSpecificationExecutor<AiConversationMessage> {

    @EntityGraph(attributePaths = {"citations"})
    Optional<AiConversationMessage> findWithCitationsById(UUID id);

    List<AiConversationMessage> findByConversationIdOrderByCreatedAtAsc(UUID conversationId);

    Optional<com.acciobuild.ai.domain.projection.ConversationMessageSummary> findSummaryById(UUID id);

    List<com.acciobuild.ai.domain.projection.ConversationMessageSummary> findSummariesByConversationId(UUID conversationId);
}
