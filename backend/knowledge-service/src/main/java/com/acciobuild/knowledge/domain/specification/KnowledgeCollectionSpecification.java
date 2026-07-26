package com.acciobuild.knowledge.domain.specification;

import com.acciobuild.knowledge.domain.model.KnowledgeCollection;
import org.springframework.data.jpa.domain.Specification;

/**
 * Dynamic specifications for querying Knowledge Collections.
 */
public final class KnowledgeCollectionSpecification {

    private KnowledgeCollectionSpecification() {}

    /**
     * Matches collections by name contains.
     */
    public static Specification<KnowledgeCollection> hasName(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return null;
            return cb.like(cb.lower(root.get("name")), "%" + keyword.toLowerCase().trim() + "%");
        };
    }
}
