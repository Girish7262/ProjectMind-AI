package com.acciobuild.ai.domain.repository;

import com.acciobuild.ai.domain.model.AiPromptTemplate;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository for AiPromptTemplate.
 */
@Repository
public interface AiPromptTemplateRepository extends JpaRepository<AiPromptTemplate, UUID>, JpaSpecificationExecutor<AiPromptTemplate> {
    
    Optional<AiPromptTemplate> findByName(String name);

    @EntityGraph(attributePaths = {"versions"})
    Optional<AiPromptTemplate> findWithVersionsByName(String name);

    Optional<com.acciobuild.ai.domain.projection.PromptTemplateSummary> findSummaryById(UUID id);
}
