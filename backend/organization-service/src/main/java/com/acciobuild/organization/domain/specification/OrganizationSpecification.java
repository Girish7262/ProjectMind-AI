package com.acciobuild.organization.domain.specification;

import com.acciobuild.organization.domain.model.Organization;
import com.acciobuild.organization.enums.OrganizationStatus;
import org.springframework.data.jpa.domain.Specification;

/**
 * JPA Specifications helper class for dynamic Organization database queries filtering.
 */
public final class OrganizationSpecification {

    private OrganizationSpecification() {}

    /**
     * Filters organizations matching keyword search query (checks code, name, and display name case-insensitively).
     */
    public static Specification<Organization> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return cb.conjunction();
            }
            String searchPattern = "%" + keyword.toLowerCase().trim() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("organizationCode")), searchPattern),
                    cb.like(cb.lower(root.get("organizationName")), searchPattern),
                    cb.like(cb.lower(root.get("displayName")), searchPattern)
            );
        };
    }

    /**
     * Filters organizations by operational status.
     */
    public static Specification<Organization> hasStatus(OrganizationStatus status) {
        return (root, query, cb) -> status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }

    /**
     * Filters organizations by country.
     */
    public static Specification<Organization> hasCountry(String country) {
        return (root, query, cb) -> (country == null || country.trim().isEmpty()) 
                ? cb.conjunction() 
                : cb.equal(cb.lower(root.get("country")), country.toLowerCase().trim());
    }

    /**
     * Filters organizations by industry sector.
     */
    public static Specification<Organization> hasIndustry(String industry) {
        return (root, query, cb) -> (industry == null || industry.trim().isEmpty()) 
                ? cb.conjunction() 
                : cb.equal(cb.lower(root.get("industry")), industry.toLowerCase().trim());
    }
}
