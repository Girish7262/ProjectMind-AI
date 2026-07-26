package com.acciobuild.knowledge.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import com.acciobuild.knowledge.enums.EmbeddingJobStatus;

/**
 * JPA Entity representing an embedding generation job for a document.
 */
@Entity
@Table(name = "embedding_jobs")
@Getter
@Setter
public class EmbeddingJob implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false, foreignKey = @ForeignKey(name = "fk_embedding_job_document"))
    private KnowledgeDocument document;

    @Column(name = "provider", nullable = false, length = 50)
    private String provider;

    @Column(name = "model", nullable = false, length = 50)
    private String model;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EmbeddingJobStatus status = EmbeddingJobStatus.PENDING;

    @Column(name = "retry_count", nullable = false)
    private int retryCount = 0;

    @Column(name = "estimated_tokens", nullable = false)
    private int estimatedTokens = 0;

    @Column(name = "estimated_cost", nullable = false)
    private double estimatedCost = 0.0;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
