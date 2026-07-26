package com.acciobuild.project.domain.specification;

import com.acciobuild.project.domain.model.Project;
import com.acciobuild.project.domain.model.ProjectTag;
import com.acciobuild.project.enums.ProjectStatus;
import com.acciobuild.project.enums.ProjectVisibility;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Utility class compiling dynamic JPA Specifications for Project queries.
 */
public final class ProjectSpecification {

    private ProjectSpecification() {}

    /**
     * Filters projects matching a keyword (case-insensitive contains on code, name, description).
     */
    public static Specification<Project> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return null;
            }
            String match = "%" + keyword.toLowerCase().trim() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("projectCode")), match),
                    cb.like(cb.lower(root.get("projectName")), match),
                    cb.like(cb.lower(root.get("displayName")), match),
                    cb.like(cb.lower(root.get("description")), match)
            );
        };
    }

    /**
     * Filters projects by operational status.
     */
    public static Specification<Project> hasStatus(ProjectStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    /**
     * Filters projects by visibility.
     */
    public static Specification<Project> hasVisibility(ProjectVisibility visibility) {
        return (root, query, cb) -> visibility == null ? null : cb.equal(root.get("visibility"), visibility);
    }

    /**
     * Filters projects created by a specific user.
     */
    public static Specification<Project> createdBy(UUID userId) {
        return (root, query, cb) -> userId == null ? null : cb.equal(root.get("createdBy"), userId);
    }

    /**
     * Filters projects by date ranges.
     */
    public static Specification<Project> createdBetween(LocalDateTime start, LocalDateTime end) {
        return (root, query, cb) -> {
            if (start == null && end == null) return null;
            if (start != null && end != null) return cb.between(root.get("createdAt"), start, end);
            if (start != null) return cb.greaterThanOrEqualTo(root.get("createdAt"), start);
            return cb.lessThanOrEqualTo(root.get("createdAt"), end);
        };
    }

    /**
     * Filters projects that are associated with a specific tag name.
     */
    public static Specification<Project> hasTag(String tagName) {
        return (root, query, cb) -> {
            if (tagName == null || tagName.isBlank()) return null;
            Join<Project, ProjectTag> tagJoin = root.join("tags");
            return cb.equal(cb.lower(tagJoin.get("tagName")), tagName.toLowerCase().trim());
        };
    }
}
