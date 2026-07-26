package com.acciobuild.knowledge.domain.repository;

import com.acciobuild.knowledge.domain.model.KnowledgeDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data Repository for KnowledgeDocument entity operations.
 */
@Repository
public interface KnowledgeDocumentRepository extends JpaRepository<KnowledgeDocument, UUID>, JpaSpecificationExecutor<KnowledgeDocument> {

    /**
     * Finds a document by project ID and unique slug parameter.
     */
    Optional<KnowledgeDocument> findByProjectIdAndSlug(UUID projectId, String slug);

    /**
     * Checks if a slug is already taken within a project boundary.
     */
    boolean existsByProjectIdAndSlug(UUID projectId, String slug);
}
