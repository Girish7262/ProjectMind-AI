package com.acciobuild.ai.domain.repository;

import com.acciobuild.ai.domain.model.AiToolExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.UUID;

/**
 * Spring Data JPA Repository for AiToolExecution.
 */
@Repository
public interface AiToolExecutionRepository extends JpaRepository<AiToolExecution, UUID>, JpaSpecificationExecutor<AiToolExecution> {
}
