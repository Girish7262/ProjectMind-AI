package com.acciobuild.knowledge.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity representing immutable versions log history for a Knowledge Document.
 */
@Entity
@Table(name = "knowledge_versions")
@Getter
@Setter
public class KnowledgeVersion implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false, foreignKey = @ForeignKey(name = "fk_version_document"))
    private KnowledgeDocument document;

    @Column(name = "version_number", nullable = false)
    private int versionNumber;

    @Column(name = "content_hash", nullable = false, length = 64)
    private String contentHash;

    @Column(name = "storage_location", nullable = false, length = 250)
    private String storageLocation;

    @Column(name = "change_summary", length = 250)
    private String changeSummary;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;
}
