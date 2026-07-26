package com.acciobuild.project.domain.repository;

import com.acciobuild.project.domain.model.RepositorySyncHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data Repository for RepositorySyncHistory.
 */
@Repository
public interface RepositorySyncHistoryRepository extends JpaRepository<RepositorySyncHistory, UUID> {

    /**
     * Lists sync history records for a repository ordered by started time descending.
     */
    List<RepositorySyncHistory> findByRepositoryIdOrderByStartedAtDesc(UUID repositoryId);
}
