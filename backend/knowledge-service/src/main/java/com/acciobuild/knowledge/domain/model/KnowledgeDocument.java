package com.acciobuild.knowledge.domain.model;

import com.acciobuild.knowledge.enums.KnowledgeSourceType;
import com.acciobuild.knowledge.enums.KnowledgeStatus;
import com.acciobuild.knowledge.enums.KnowledgeVisibility;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Aggregate root representing a system Knowledge Document containing metadata,
 * versions log, attachments, and collections bindings.
 */
@Entity
@Table(name = "knowledge_documents", uniqueConstraints = {
        @UniqueConstraint(name = "uq_doc_slug_project", columnNames = {"project_id", "slug"})
})
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = UUID.class))
@Filter(name = "tenantFilter", condition = "organization_id = :tenantId")
@Getter
@Setter
public class KnowledgeDocument implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "slug", nullable = false, length = 150)
    private String slug;

    @Column(name = "summary", length = 500)
    private String summary;

    @Column(name = "content_type", nullable = false, length = 50)
    private String contentType;

    @Column(name = "content_format", nullable = false, length = 50)
    private String contentFormat = "markdown";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private KnowledgeStatus status = KnowledgeStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false, length = 20)
    private KnowledgeVisibility visibility = KnowledgeVisibility.INTERNAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 20)
    private KnowledgeSourceType sourceType = KnowledgeSourceType.MANUAL;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "updated_by", nullable = false)
    private UUID updatedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<KnowledgeVersion> versions = new HashSet<>();

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<KnowledgeAttachment> attachments = new HashSet<>();

    @OneToOne(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    private KnowledgeMetadata metadata;
}
