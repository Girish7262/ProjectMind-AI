package com.acciobuild.knowledge.domain.model;

import com.acciobuild.knowledge.enums.RelationshipType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import java.io.Serializable;
import java.util.UUID;

/**
 * JPA Entity mapping typed semantic relations between Knowledge Documents.
 */
@Entity
@Table(name = "knowledge_relationships", uniqueConstraints = {
        @UniqueConstraint(name = "uq_relation_src_tgt", columnNames = {"source_document_id", "target_document_id"})
})
@Filter(name = "tenantFilter", condition = "source_document_id IN (SELECT d.id FROM knowledge_documents d WHERE d.organization_id = :tenantId)")
@Getter
@Setter
public class KnowledgeRelationship implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_document_id", nullable = false, foreignKey = @ForeignKey(name = "fk_relation_source"))
    private KnowledgeDocument sourceDocument;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_document_id", nullable = false, foreignKey = @ForeignKey(name = "fk_relation_target"))
    private KnowledgeDocument targetDocument;

    @Enumerated(EnumType.STRING)
    @Column(name = "relationship_type", nullable = false, length = 20)
    private RelationshipType relationshipType;

    @Column(name = "strength", nullable = false)
    private double strength = 1.0;
}
