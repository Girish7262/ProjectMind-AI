package com.acciobuild.organization.domain.repository;

import com.acciobuild.organization.domain.model.Organization;
import com.acciobuild.organization.dto.projection.OrganizationSummary;
import com.acciobuild.organization.enums.OrganizationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository managing transaction operations against Organizations table.
 * Employs Specifications for dynamic filters and Entity Graphs for performance tuning.
 */
@Repository
public interface OrganizationRepository extends JpaRepository<Organization, UUID>, JpaSpecificationExecutor<Organization> {

    /**
     * Retrieve organization matching unique code identifier.
     */
    Optional<Organization> findByOrganizationCode(String organizationCode);

    /**
     * Retrieve organization matching unique name.
     */
    Optional<Organization> findByOrganizationName(String organizationName);

    /**
     * Verify existence of organization matching unique code.
     */
    boolean existsByOrganizationCode(String organizationCode);

    /**
     * Verify existence of organization matching unique name.
     */
    boolean existsByOrganizationName(String organizationName);

    /**
     * Eagerly fetches an organization along with its settings using an Entity Graph.
     * Prevents N+1 query problems.
     */
    @EntityGraph(attributePaths = {"settings"})
    Optional<Organization> findWithSettingsById(UUID id);

    /**
     * Eagerly fetches an organization along with its settings by organization code.
     */
    @EntityGraph(attributePaths = {"settings"})
    Optional<Organization> findWithSettingsByOrganizationCode(String organizationCode);

    /**
     * Retrieve lightweight projections of organizations matching status constraint.
     */
    Page<OrganizationSummary> findSummariesByStatus(OrganizationStatus status, Pageable pageable);
}
