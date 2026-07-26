package com.acciobuild.knowledge.domain.specification;

import com.acciobuild.knowledge.domain.model.KnowledgeTag;
import org.springframework.data.jpa.domain.Specification;

/**
 * Dynamic specifications for querying Knowledge Tags.
 */
public final class KnowledgeTagSpecification {

    private KnowledgeTagSpecification() {}

    /**
     * Matches tags by name contains.
     */
    public static Specification<KnowledgeTag> hasName(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return null;
            return cb.like(cb.lower(root.get("name")), "%" + keyword.toLowerCase().trim() + "%");
        };
    }
}
