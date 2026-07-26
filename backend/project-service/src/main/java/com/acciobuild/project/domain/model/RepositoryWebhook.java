package com.acciobuild.project.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity mapping webhooks configured for connected Git repositories.
 */
@Entity
@Table(name = "repository_webhooks")
@Filter(name = "tenantFilter", condition = "repository_id IN (SELECT r.id FROM git_repositories r JOIN projects p ON r.project_id = p.id WHERE p.organization_id = :tenantId)")
@Getter
@Setter
public class RepositoryWebhook implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id", nullable = false, foreignKey = @ForeignKey(name = "fk_repo_webhook_repo"))
    private GitRepository repository;

    @Column(name = "webhook_url", nullable = false, length = 250)
    private String webhookUrl;

    @Column(name = "secret", nullable = false, length = 100)
    private String secret;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @Column(name = "last_triggered_at")
    private LocalDateTime lastTriggeredAt;
}
