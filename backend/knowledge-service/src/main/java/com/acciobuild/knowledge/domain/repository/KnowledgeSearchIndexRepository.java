package com.acciobuild.knowledge.domain.repository;

import com.acciobuild.knowledge.domain.model.KnowledgeSearchIndex;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

/**
 * Spring Data Repository for KnowledgeSearchIndex.
 */
@Repository
public interface KnowledgeSearchIndexRepository extends JpaRepository<KnowledgeSearchIndex, UUID> {
}
