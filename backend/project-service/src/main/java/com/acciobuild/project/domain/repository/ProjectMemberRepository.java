package com.acciobuild.project.domain.repository;

import com.acciobuild.project.domain.model.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository managing project membership database transactions.
 */
@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, UUID> {

    /**
     * Resolves all membership records associated with a project.
     */
    List<ProjectMember> findByProjectId(UUID projectId);

    /**
     * Resolves user membership details inside a project.
     */
    Optional<ProjectMember> findByProjectIdAndUserId(UUID projectId, UUID userId);

    /**
     * Checks if a user is already enrolled inside a project.
     */
    boolean existsByProjectIdAndUserId(UUID projectId, UUID userId);
}
