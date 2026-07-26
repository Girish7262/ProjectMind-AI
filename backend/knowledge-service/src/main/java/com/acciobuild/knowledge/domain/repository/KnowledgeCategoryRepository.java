package com.acciobuild.knowledge.domain.repository;

import com.acciobuild.knowledge.domain.model.KnowledgeCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data Repository for KnowledgeCategory entity.
 */
@Repository
public interface KnowledgeCategoryRepository extends JpaRepository<KnowledgeCategory, UUID> {

    /**
     * Lists categories registered inside a specific project.
     */
    List<KnowledgeCategory> findByProjectId(UUID projectId);
}
