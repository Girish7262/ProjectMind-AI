package com.acciobuild.ai.domain.repository;

import com.acciobuild.ai.domain.model.AiPromptVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA Repository for AiPromptVersion.
 */
@Repository
public interface AiPromptVersionRepository extends JpaRepository<AiPromptVersion, UUID>, JpaSpecificationExecutor<AiPromptVersion> {
    List<AiPromptVersion> findByTemplateId(UUID templateId);
}
