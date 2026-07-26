package com.acciobuild.project.domain.specification;

import com.acciobuild.project.domain.model.ProjectMember;
import com.acciobuild.project.enums.ProjectMemberRole;
import org.springframework.data.jpa.domain.Specification;
import java.util.UUID;

/**
 * Utility class compiling dynamic JPA Specifications for ProjectMember queries.
 */
public final class ProjectMemberSpecification {

    private ProjectMemberSpecification() {}

    /**
     * Filters members by role.
     */
    public static Specification<ProjectMember> hasRole(ProjectMemberRole role) {
        return (root, query, cb) -> role == null ? null : cb.equal(root.get("role"), role);
    }

    /**
     * Filters members by status.
     */
    public static Specification<ProjectMember> hasStatus(String status) {
        return (root, query, cb) -> {
            if (status == null || status.isBlank()) return null;
            return cb.equal(cb.lower(root.get("status")), status.toLowerCase().trim());
        };
    }

    /**
     * Filters members by specific user ID.
     */
    public static Specification<ProjectMember> hasUserId(UUID userId) {
        return (root, query, cb) -> userId == null ? null : cb.equal(root.get("userId"), userId);
    }
}
