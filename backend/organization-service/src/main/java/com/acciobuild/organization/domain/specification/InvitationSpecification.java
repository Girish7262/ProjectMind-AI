package com.acciobuild.organization.domain.specification;

import com.acciobuild.organization.domain.model.OrganizationInvitation;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Specifications helper class for dynamic OrganizationInvitation database queries filtering.
 */
public final class InvitationSpecification {

    private InvitationSpecification() {}

    /**
     * Filters invitations by parent organization.
     */
    public static Specification<OrganizationInvitation> hasOrganizationId(UUID organizationId) {
        return (root, query, cb) -> organizationId == null 
                ? cb.conjunction() 
                : cb.equal(root.get("organization").get("id"), organizationId);
    }

    /**
     * Filters invitations by recipient email address search query.
     */
    public static Specification<OrganizationInvitation> hasEmail(String email) {
        return (root, query, cb) -> (email == null || email.trim().isEmpty()) 
                ? cb.conjunction() 
                : cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase().trim() + "%");
    }

    /**
     * Filters invitations by accepted status flag.
     */
    public static Specification<OrganizationInvitation> isAccepted(Boolean accepted) {
        return (root, query, cb) -> accepted == null ? cb.conjunction() : cb.equal(root.get("accepted"), accepted);
    }

    /**
     * Filters invitations by expiration state.
     */
    public static Specification<OrganizationInvitation> isExpired(Boolean expired) {
        return (root, query, cb) -> {
            if (expired == null) {
                return cb.conjunction();
            }
            LocalDateTime now = LocalDateTime.now();
            return expired 
                    ? cb.and(cb.lessThan(root.get("expiresAt"), now), cb.equal(root.get("accepted"), false))
                    : cb.greaterThan(root.get("expiresAt"), now);
        };
    }
}
