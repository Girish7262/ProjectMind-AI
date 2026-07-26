package com.acciobuild.knowledge.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity representing the text corpus prepared for full-text search index matching.
 */
@Entity
@Table(name = "knowledge_search_index")
@Getter
@Setter
public class KnowledgeSearchIndex implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false, foreignKey = @ForeignKey(name = "fk_search_index_document"))
    private KnowledgeDocument document;

    @Lob
    @Column(name = "search_text", nullable = false)
    private String searchText;

    @Column(name = "weight", nullable = false)
    private double weight = 1.0;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
}
