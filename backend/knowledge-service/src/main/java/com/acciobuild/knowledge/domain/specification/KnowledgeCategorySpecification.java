package com.acciobuild.knowledge.domain.specification;

import com.acciobuild.knowledge.domain.model.KnowledgeCategory;
import org.springframework.data.jpa.domain.Specification;

/**
 * Dynamic specifications for querying Knowledge Categories.
 */
public final class KnowledgeCategorySpecification {

    private KnowledgeCategorySpecification() {}

    /**
     * Matches categories by name contains.
     */
    public static Specification<KnowledgeCategory> hasName(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return null;
            return cb.like(cb.lower(root.get("name")), "%" + keyword.toLowerCase().trim() + "%");
        };
    }
}
