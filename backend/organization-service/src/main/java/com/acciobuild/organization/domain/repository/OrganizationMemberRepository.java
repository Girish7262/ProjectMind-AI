package com.acciobuild.organization.domain.repository;

import com.acciobuild.organization.domain.model.OrganizationMember;
import com.acciobuild.organization.enums.MemberRole;
import com.acciobuild.organization.enums.MemberStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository managing transaction operations against OrganizationMembers table.
 * Supports specification executers for dynamic search filters, pagination, and sorting.
 */
@Repository
public interface OrganizationMemberRepository extends JpaRepository<OrganizationMember, UUID>, JpaSpecificationExecutor<OrganizationMember> {

    /**
     * Retrieve members mapped to a specific organization.
     */
    List<OrganizationMember> findByOrganizationId(UUID organizationId);

    /**
     * Paginates members mapped to a specific organization.
     */
    Page<OrganizationMember> findByOrganizationId(UUID organizationId, Pageable pageable);

    /**
     * Find membership details by user ID.
     */
    List<OrganizationMember> findByUserId(UUID userId);

    /**
     * Find specific membership details mapping organization and user constraints.
     */
    Optional<OrganizationMember> findByOrganizationIdAndUserId(UUID organizationId, UUID userId);

    /**
     * Verify user membership mapping.
     */
    boolean existsByOrganizationIdAndUserId(UUID organizationId, UUID userId);

    /**
     * Count members mapped to an organization.
     */
    long countByOrganizationId(UUID organizationId);

    /**
     * Find members of an organization filtering by status.
     */
    List<OrganizationMember> findByOrganizationIdAndStatus(UUID organizationId, MemberStatus status);

    /**
     * Find members of an organization filtering by role.
     */
    List<OrganizationMember> findByOrganizationIdAndRole(UUID organizationId, MemberRole role);

    /**
     * Paginates members along with parent organization eager fetched.
     */
    @EntityGraph(attributePaths = {"organization"})
    Page<OrganizationMember> findWithOrganizationByOrganizationId(UUID organizationId, Pageable pageable);
}
