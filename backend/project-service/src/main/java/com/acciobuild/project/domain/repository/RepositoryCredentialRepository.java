package com.acciobuild.project.domain.repository;

import com.acciobuild.project.domain.model.RepositoryCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

/**
 * Spring Data Repository for RepositoryCredential entity.
 */
@Repository
public interface RepositoryCredentialRepository extends JpaRepository<RepositoryCredential, UUID> {
}
