package com.acciobuild.ai.domain.repository;

import com.acciobuild.ai.domain.model.AiToolDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.UUID;

/**
 * Spring Data JPA Repository for AiToolDefinition.
 */
@Repository
public interface AiToolDefinitionRepository extends JpaRepository<AiToolDefinition, UUID>, JpaSpecificationExecutor<AiToolDefinition> {
}
