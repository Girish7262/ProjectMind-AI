package com.acciobuild.knowledge.domain.repository;

import com.acciobuild.knowledge.domain.model.KnowledgeRelationship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data Repository for KnowledgeRelationship entity.
 */
@Repository
public interface KnowledgeRelationshipRepository extends JpaRepository<KnowledgeRelationship, UUID> {

    /**
     * Lists relations originating from a source document.
     */
    List<KnowledgeRelationship> findBySourceDocumentId(UUID sourceDocumentId);
}
