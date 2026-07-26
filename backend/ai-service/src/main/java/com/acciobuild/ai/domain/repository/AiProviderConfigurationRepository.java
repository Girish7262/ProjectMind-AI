package com.acciobuild.ai.domain.repository;

import com.acciobuild.ai.domain.model.AiProviderConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.UUID;

/**
 * Spring Data JPA Repository for AiProviderConfiguration.
 */
@Repository
public interface AiProviderConfigurationRepository extends JpaRepository<AiProviderConfiguration, UUID>, JpaSpecificationExecutor<AiProviderConfiguration> {
}
