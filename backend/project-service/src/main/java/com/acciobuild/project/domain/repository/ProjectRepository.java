package com.acciobuild.project.domain.repository;

import com.acciobuild.project.domain.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository managing Project aggregate database queries and specifications.
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID>, JpaSpecificationExecutor<Project> {

    /**
     * Finds a project by its unique code.
     */
    Optional<Project> findByProjectCode(String projectCode);

    /**
     * Eagerly loads a project along with its settings aggregate.
     */
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"settings"})
    Optional<Project> findWithSettingsById(UUID id);

    /**
     * Checks if a project code already exists.
     */
    boolean existsByProjectCode(String projectCode);

    /**
     * Checks if a project name collision exists within the organization tenant.
     */
    boolean existsByOrganizationIdAndProjectName(UUID organizationId, String projectName);

    /**
     * Counts the total number of projects provisioned in an organization.
     */
    long countByOrganizationId(UUID organizationId);
}
