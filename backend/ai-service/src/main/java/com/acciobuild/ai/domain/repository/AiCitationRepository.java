package com.acciobuild.ai.domain.repository;

import com.acciobuild.ai.domain.model.AiCitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository for AiCitation.
 */
@Repository
public interface AiCitationRepository extends JpaRepository<AiCitation, UUID>, JpaSpecificationExecutor<AiCitation> {
    
    List<AiCitation> findByMessageId(UUID messageId);

    Optional<com.acciobuild.ai.domain.projection.CitationSummary> findSummaryById(UUID id);

    List<com.acciobuild.ai.domain.projection.CitationSummary> findSummariesByMessageId(UUID messageId);
}
