package com.acciobuild.knowledge.domain.specification;

import com.acciobuild.knowledge.domain.model.KnowledgeRelationship;
import com.acciobuild.knowledge.enums.RelationshipType;
import org.springframework.data.jpa.domain.Specification;
import java.util.UUID;

/**
 * Dynamic specifications for querying Knowledge Relationships.
 */
public final class KnowledgeRelationshipSpecification {

    private KnowledgeRelationshipSpecification() {}

    /**
     * Filters relationships matching a relationship type.
     */
    public static Specification<KnowledgeRelationship> hasType(RelationshipType relationshipType) {
        return (root, query, cb) -> relationshipType == null ? null : cb.equal(root.get("relationshipType"), relationshipType);
    }

    /**
     * Filters relationships originating from a source document.
     */
    public static Specification<KnowledgeRelationship> hasSource(UUID sourceDocumentId) {
        return (root, query, cb) -> sourceDocumentId == null ? null : cb.equal(root.get("sourceDocument").get("id"), sourceDocumentId);
    }
}
