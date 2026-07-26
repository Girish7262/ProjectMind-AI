package com.acciobuild.project.domain.model;

import com.acciobuild.project.enums.GitProvider;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity representing registered Git repositories.
 */
@Entity
@Table(name = "git_repositories")
@Filter(name = "tenantFilter", condition = "project_id IN (SELECT p.id FROM projects p WHERE p.organization_id = :tenantId)")
@Getter
@Setter
public class GitRepository implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false, foreignKey = @ForeignKey(name = "fk_git_repo_project"))
    private Project project;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 20)
    private GitProvider provider;

    @Column(name = "repository_name", nullable = false, length = 100)
    private String repositoryName;

    @Column(name = "repository_url", nullable = false, length = 250)
    private String repositoryUrl;

    @Column(name = "default_branch", nullable = false, length = 50)
    private String defaultBranch = "main";

    @Column(name = "status", nullable = false, length = 20)
    private String status = "ACTIVE";

    @Column(name = "visibility", nullable = false, length = 20)
    private String visibility = "PRIVATE";

    @Column(name = "is_archived", nullable = false)
    private boolean isArchived = false;

    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
}
