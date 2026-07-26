package com.acciobuild.knowledge.domain.repository;

import com.acciobuild.knowledge.domain.model.KnowledgeTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data Repository for KnowledgeTag entity.
 */
@Repository
public interface KnowledgeTagRepository extends JpaRepository<KnowledgeTag, UUID> {

    /**
     * Lists tags defined inside a specific project.
     */
    List<KnowledgeTag> findByProjectId(UUID projectId);
}
