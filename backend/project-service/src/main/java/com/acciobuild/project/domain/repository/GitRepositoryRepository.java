package com.acciobuild.project.domain.repository;

import com.acciobuild.project.domain.model.GitRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data Repository for GitRepository entity operations.
 */
@Repository
public interface GitRepositoryRepository extends JpaRepository<GitRepository, UUID>, JpaSpecificationExecutor<GitRepository> {

    /**
     * Checks if a repository URL has already been registered inside a project.
     */
    boolean existsByProjectIdAndRepositoryUrl(UUID projectId, String repositoryUrl);

    /**
     * Finds repositories enrolled inside a specific project.
     */
    List<GitRepository> findByProjectId(UUID projectId);
}
