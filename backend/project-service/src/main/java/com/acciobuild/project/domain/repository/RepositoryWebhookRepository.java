package com.acciobuild.project.domain.repository;

import com.acciobuild.project.domain.model.RepositoryWebhook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data Repository for RepositoryWebhook entity.
 */
@Repository
public interface RepositoryWebhookRepository extends JpaRepository<RepositoryWebhook, UUID> {

    /**
     * Finds webhook configurations for a specific repository.
     */
    Optional<RepositoryWebhook> findByRepositoryId(UUID repositoryId);
}
