package com.acciobuild.knowledge.domain.repository;

import com.acciobuild.knowledge.domain.model.EmbeddingProviderConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data Repository for EmbeddingProviderConfig.
 */
@Repository
public interface EmbeddingProviderConfigRepository extends JpaRepository<EmbeddingProviderConfig, UUID> {

    /**
     * Finds active config by provider name.
     */
    Optional<EmbeddingProviderConfig> findByProviderNameAndIsActiveTrue(String providerName);
}
