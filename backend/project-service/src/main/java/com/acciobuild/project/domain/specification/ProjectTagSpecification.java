package com.acciobuild.project.domain.specification;

import com.acciobuild.project.domain.model.ProjectTag;
import org.springframework.data.jpa.domain.Specification;

/**
 * Utility class compiling dynamic JPA Specifications for ProjectTag queries.
 */
public final class ProjectTagSpecification {

    private ProjectTagSpecification() {}

    /**
     * Filters tags by matching name keyword.
     */
    public static Specification<ProjectTag> hasName(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return null;
            return cb.like(cb.lower(root.get("tagName")), "%" + keyword.toLowerCase().trim() + "%");
        };
    }

    /**
     * Filters tags by color HEX code.
     */
    public static Specification<ProjectTag> hasColor(String color) {
        return (root, query, cb) -> {
            if (color == null || color.isBlank()) return null;
            return cb.equal(cb.lower(root.get("color")), color.toLowerCase().trim());
        };
    }
}
