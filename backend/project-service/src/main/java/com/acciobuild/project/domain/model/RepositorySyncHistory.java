package com.acciobuild.project.domain.model;

import com.acciobuild.project.enums.SyncStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity tracking repository synchronization operations history.
 */
@Entity
@Table(name = "repository_sync_history")
@Filter(name = "tenantFilter", condition = "repository_id IN (SELECT r.id FROM git_repositories r JOIN projects p ON r.project_id = p.id WHERE p.organization_id = :tenantId)")
@Getter
@Setter
public class RepositorySyncHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id", nullable = false, foreignKey = @ForeignKey(name = "fk_repo_sync_repo"))
    private GitRepository repository;

    @Enumerated(EnumType.STRING)
    @Column(name = "sync_status", nullable = false, length = 20)
    private SyncStatus syncStatus;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt = LocalDateTime.now();

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "commit_count")
    private int commitCount = 0;

    @Column(name = "branch_count")
    private int branchCount = 0;

    @Column(name = "error_message", length = 500)
    private String errorMessage;
}
