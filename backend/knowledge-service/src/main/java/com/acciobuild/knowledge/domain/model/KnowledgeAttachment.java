package com.acciobuild.knowledge.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import java.io.Serializable;
import java.util.UUID;

/**
 * JPA Entity representing binary files uploaded and attached to a Knowledge Document.
 */
@Entity
@Table(name = "knowledge_attachments")
@Filter(name = "tenantFilter", condition = "document_id IN (SELECT d.id FROM knowledge_documents d WHERE d.organization_id = :tenantId)")
@Getter
@Setter
public class KnowledgeAttachment implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false, foreignKey = @ForeignKey(name = "fk_attachment_document"))
    private KnowledgeDocument document;

    @Column(name = "file_name", nullable = false, length = 150)
    private String fileName;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "storage_path", nullable = false, length = 250)
    private String storagePath;

    @Column(name = "size_bytes", nullable = false)
    private long size;

    @Column(name = "checksum", nullable = false, length = 64)
    private String checksum;
}
