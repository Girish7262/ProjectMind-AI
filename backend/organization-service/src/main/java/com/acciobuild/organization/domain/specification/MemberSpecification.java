package com.acciobuild.organization.domain.specification;

import com.acciobuild.organization.domain.model.OrganizationMember;
import com.acciobuild.organization.enums.MemberRole;
import com.acciobuild.organization.enums.MemberStatus;
import org.springframework.data.jpa.domain.Specification;
import java.util.UUID;

/**
 * JPA Specifications helper class for dynamic OrganizationMember database queries filtering.
 */
public final class MemberSpecification {

    private MemberSpecification() {}

    /**
     * Filters members by parent organization.
     */
    public static Specification<OrganizationMember> hasOrganizationId(UUID organizationId) {
        return (root, query, cb) -> organizationId == null 
                ? cb.conjunction() 
                : cb.equal(root.get("organization").get("id"), organizationId);
    }

    /**
     * Filters members by user ID reference.
     */
    public static Specification<OrganizationMember> hasUserId(UUID userId) {
        return (root, query, cb) -> userId == null ? cb.conjunction() : cb.equal(root.get("userId"), userId);
    }

    /**
     * Filters members by membership role.
     */
    public static Specification<OrganizationMember> hasRole(MemberRole role) {
        return (root, query, cb) -> role == null ? cb.conjunction() : cb.equal(root.get("role"), role);
    }

    /**
     * Filters members by operational enrollment status.
     */
    public static Specification<OrganizationMember> hasStatus(MemberStatus status) {
        return (root, query, cb) -> status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }
}
