package com.acciobuild.knowledge.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import java.io.Serializable;
import java.util.UUID;

/**
 * JPA Entity representing partitioned knowledge text chunks prepared for semantic vector modeling.
 */
@Entity
@Table(name = "knowledge_document_chunks")
@Filter(name = "tenantFilter", condition = "organization_id = :tenantId")
@Getter
@Setter
public class KnowledgeDocumentChunk implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false, foreignKey = @ForeignKey(name = "fk_chunk_document"))
    private KnowledgeDocument document;

    @Column(name = "chunk_index", nullable = false)
    private int chunkIndex;

    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "token_count", nullable = false)
    private int tokenCount;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "estimated_cost", nullable = false)
    private double estimatedCost = 0.0;

    @Column(name = "language", nullable = false, length = 10)
    private String language = "en";

    @Column(name = "priority", nullable = false)
    private int priority = 0;

    @Column(name = "chunk_hash", nullable = false, length = 64)
    private String chunkHash = "";

    @Column(name = "content_checksum", nullable = false, length = 64)
    private String contentChecksum = "";

    @Column(name = "embedding_eligibility", nullable = false)
    private boolean embeddingEligibility = true;

    @Column(name = "processing_status", nullable = false, length = 20)
    private String processingStatus = "PENDING";
}
