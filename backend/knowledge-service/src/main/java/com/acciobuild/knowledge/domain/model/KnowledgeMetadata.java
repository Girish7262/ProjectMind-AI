package com.acciobuild.knowledge.domain.model;

import com.acciobuild.knowledge.enums.ApprovalStatus;
import com.acciobuild.knowledge.enums.ReviewStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity storing review metadata and keyword configurations for a Knowledge Document.
 */
@Entity
@Table(name = "knowledge_metadata")
@Filter(name = "tenantFilter", condition = "document_id IN (SELECT d.id FROM knowledge_documents d WHERE d.organization_id = :tenantId)")
@Getter
@Setter
public class KnowledgeMetadata implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "document_id")
    private UUID documentId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "document_id", foreignKey = @ForeignKey(name = "fk_metadata_document"))
    private KnowledgeDocument document;

    @Column(name = "language", nullable = false, length = 10)
    private String language = "en";

    @Column(name = "keywords", length = 250)
    private String keywords;

    @Column(name = "author", length = 100)
    private String author;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_status", nullable = false, length = 20)
    private ReviewStatus reviewStatus = ReviewStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false, length = 20)
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;

    @Column(name = "last_reviewed_at")
    private LocalDateTime lastReviewedAt;
}
