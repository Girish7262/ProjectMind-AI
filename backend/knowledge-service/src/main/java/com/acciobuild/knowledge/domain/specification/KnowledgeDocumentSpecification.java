package com.acciobuild.knowledge.domain.specification;

import com.acciobuild.knowledge.domain.model.KnowledgeDocument;
import com.acciobuild.knowledge.enums.KnowledgeSourceType;
import com.acciobuild.knowledge.enums.KnowledgeStatus;
import com.acciobuild.knowledge.enums.KnowledgeVisibility;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Dynamic specifications for querying Knowledge Documents.
 */
public final class KnowledgeDocumentSpecification {

    private KnowledgeDocumentSpecification() {}

    /**
     * Matches keyword contains case-insensitively on title, slug, and summary.
     */
    public static Specification<KnowledgeDocument> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return null;
            String pattern = "%" + keyword.toLowerCase().trim() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("title")), pattern),
                    cb.like(cb.lower(root.get("slug")), pattern),
                    cb.like(cb.lower(root.get("summary")), pattern)
            );
        };
    }

    /**
     * Filters by status.
     */
    public static Specification<KnowledgeDocument> hasStatus(KnowledgeStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    /**
     * Filters by visibility.
     */
    public static Specification<KnowledgeDocument> hasVisibility(KnowledgeVisibility visibility) {
        return (root, query, cb) -> visibility == null ? null : cb.equal(root.get("visibility"), visibility);
    }

    /**
     * Filters by source type.
     */
    public static Specification<KnowledgeDocument> hasSourceType(KnowledgeSourceType sourceType) {
        return (root, query, cb) -> sourceType == null ? null : cb.equal(root.get("sourceType"), sourceType);
    }

    /**
     * Filters by project ID.
     */
    public static Specification<KnowledgeDocument> hasProjectId(UUID projectId) {
        return (root, query, cb) -> projectId == null ? null : cb.equal(root.get("projectId"), projectId);
    }

    /**
     * Filters by date ranges.
     */
    public static Specification<KnowledgeDocument> createdBetween(LocalDateTime start, LocalDateTime end) {
        return (root, query, cb) -> {
            if (start == null && end == null) return null;
            if (start != null && end != null) return cb.between(root.get("createdAt"), start, end);
            if (start != null) return cb.greaterThanOrEqualTo(root.get("createdAt"), start);
            return cb.lessThanOrEqualTo(root.get("createdAt"), end);
        };
    }
}
