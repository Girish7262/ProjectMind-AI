package com.acciobuild.project.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import java.io.Serializable;
import java.util.UUID;

/**
 * JPA Entity representing ProjectSettings limits.
 * Bound to the Project aggregate root via a MapsId 1-to-1 relationship.
 */
@Entity
@Table(name = "project_settings")
@Filter(name = "tenantFilter", condition = "project_id IN (SELECT p.id FROM projects p WHERE p.organization_id = :tenantId)")
@Getter
@Setter
public class ProjectSettings implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "project_id")
    private UUID projectId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "project_id", foreignKey = @ForeignKey(name = "fk_project_settings_project"))
    private Project project;

    @Column(name = "ai_enabled", nullable = false)
    private boolean aiEnabled = true;

    @Column(name = "knowledge_capture_enabled", nullable = false)
    private boolean knowledgeCaptureEnabled = true;

    @Column(name = "code_analysis_enabled", nullable = false)
    private boolean codeAnalysisEnabled = true;

    @Column(name = "documentation_enabled", nullable = false)
    private boolean documentationEnabled = true;

    @Column(name = "max_repositories", nullable = false)
    private int maxRepositories = 5;

    @Size(max = 50)
    @Column(name = "default_branch", nullable = false, length = 50)
    private String defaultBranch = "main";

    // Feature Flags
    @Column(name = "repository_sync_enabled", nullable = false)
    private boolean repositorySyncEnabled = true;

    @Column(name = "webhooks_enabled", nullable = false)
    private boolean webhooksEnabled = true;

    @Column(name = "cicd_enabled", nullable = false)
    private boolean ciCdEnabled = true;

    @Column(name = "api_access_enabled", nullable = false)
    private boolean apiAccessEnabled = true;

    @Column(name = "notifications_enabled", nullable = false)
    private boolean notificationsEnabled = true;

    @Column(name = "audit_logging_enabled", nullable = false)
    private boolean auditLoggingEnabled = true;

    // Settings Quotas
    @Column(name = "max_documents", nullable = false)
    private int maxDocuments = 100;

    @Column(name = "max_team_members", nullable = false)
    private int maxTeamMembers = 20;

    @Column(name = "storage_limit_gb", nullable = false)
    private int storageLimitGb = 5;

    @Column(name = "daily_ai_requests", nullable = false)
    private int dailyAiRequests = 100;

    @Size(max = 250)
    @Column(name = "webhook_url", length = 250)
    private String webhookUrl;

    @Size(max = 100)
    @Column(name = "allowed_repository_providers", length = 100)
    private String allowedRepositoryProviders = "github,gitlab";

    @Size(max = 50)
    @Column(name = "code_analysis_profile", length = 50)
    private String codeAnalysisProfile = "standard";

    @Size(max = 50)
    @Column(name = "documentation_template", length = 50)
    private String documentationTemplate = "default";
}
