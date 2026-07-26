package com.acciobuild.project.domain.repository;

import com.acciobuild.project.domain.model.ProjectTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA Repository managing project tags database transactions.
 */
@Repository
public interface ProjectTagRepository extends JpaRepository<ProjectTag, UUID> {

    /**
     * Resolves all custom tags defined within a project.
     */
    List<ProjectTag> findByProjectId(UUID projectId);
}
