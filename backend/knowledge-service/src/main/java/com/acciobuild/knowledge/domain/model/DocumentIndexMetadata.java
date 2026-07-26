package com.acciobuild.knowledge.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity capturing vector pipeline configuration and embedding statuses.
 */
@Entity
@Table(name = "document_index_metadata")
@Getter
@Setter
public class DocumentIndexMetadata implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "document_id")
    private UUID documentId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "document_id", foreignKey = @ForeignKey(name = "fk_index_metadata_document"))
    private KnowledgeDocument document;

    @Column(name = "chunk_count", nullable = false)
    private int chunkCount = 0;

    @Column(name = "estimated_token_count", nullable = false)
    private int estimatedTokenCount = 0;

    @Column(name = "content_hash", nullable = false, length = 64)
    private String contentHash;

    @Column(name = "embedding_status", nullable = false, length = 20)
    private String embeddingStatus = "PENDING";

    @Column(name = "embedding_version", nullable = false)
    private int embeddingVersion = 1;

    @Column(name = "embedding_provider", length = 50)
    private String embeddingProvider;

    @Column(name = "embedding_model", length = 50)
    private String embeddingModel;

    @Column(name = "embedding_generated_at")
    private LocalDateTime embeddingGeneratedAt;

    @Column(name = "vector_id")
    private UUID vectorId;

    @Column(name = "embedding_dimension")
    private Integer embeddingDimension;

    @Column(name = "embedding_checksum", length = 64)
    private String embeddingChecksum;

    @Column(name = "embedding_updated_at")
    private LocalDateTime embeddingUpdatedAt;
}
