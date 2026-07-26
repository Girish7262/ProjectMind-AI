package com.acciobuild.knowledge.domain.repository;

import com.acciobuild.knowledge.domain.model.KnowledgeCollection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data Repository for KnowledgeCollection entity.
 */
@Repository
public interface KnowledgeCollectionRepository extends JpaRepository<KnowledgeCollection, UUID> {

    /**
     * Lists document collections mapped inside a specific project.
     */
    List<KnowledgeCollection> findByProjectId(UUID projectId);
}
