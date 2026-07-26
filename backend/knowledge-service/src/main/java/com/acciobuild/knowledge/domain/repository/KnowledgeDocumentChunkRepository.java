package com.acciobuild.knowledge.domain.repository;

import com.acciobuild.knowledge.domain.model.KnowledgeDocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data Repository for KnowledgeDocumentChunk.
 */
@Repository
public interface KnowledgeDocumentChunkRepository extends JpaRepository<KnowledgeDocumentChunk, UUID> {

    /**
     * Lists chunk fragments belonging to a document ID.
     */
    List<KnowledgeDocumentChunk> findByDocumentIdOrderByChunkIndexAsc(UUID documentId);
}
