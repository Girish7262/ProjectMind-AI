package com.acciobuild.project.domain.repository;

import com.acciobuild.project.domain.model.ProjectSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

/**
 * Spring Data JPA Repository managing project settings database transactions.
 */
@Repository
public interface ProjectSettingsRepository extends JpaRepository<ProjectSettings, UUID> {
}
