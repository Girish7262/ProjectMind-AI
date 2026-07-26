package com.acciobuild.project.domain.model;

import com.acciobuild.project.enums.CredentialType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity storing credentials for private Git repositories.
 */
@Entity
@Table(name = "repository_credentials")
@Filter(name = "tenantFilter", condition = "repository_id IN (SELECT r.id FROM git_repositories r JOIN projects p ON r.project_id = p.id WHERE p.organization_id = :tenantId)")
@Getter
@Setter
public class RepositoryCredential implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "repository_id")
    private UUID repositoryId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "repository_id", foreignKey = @ForeignKey(name = "fk_repo_cred_repo"))
    private GitRepository repository;

    @Enumerated(EnumType.STRING)
    @Column(name = "credential_type", nullable = false, length = 20)
    private CredentialType credentialType;

    @Column(name = "encrypted_token", length = 500)
    private String encryptedToken;

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
}
