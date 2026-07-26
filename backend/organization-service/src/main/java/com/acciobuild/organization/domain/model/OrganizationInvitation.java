package com.acciobuild.organization.domain.model;

import com.acciobuild.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity representing the OrganizationInvitation aggregate root.
 * Integrates with Hibernate's tenantFilter to ensure multi-tenant data isolation.
 */
@Entity
@Table(
        name = "organization_invitations",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"invite_token"}, name = "uk_organization_invitations_token")
        },
        indexes = {
                @Index(columnList = "invite_token", name = "idx_organization_invitations_token"),
                @Index(columnList = "organization_id", name = "idx_organization_invitations_org"),
                @Index(columnList = "email", name = "idx_organization_invitations_email")
        }
)
@Filter(name = "tenantFilter", condition = "organization_id = :tenantId")
@Getter
@Setter
public class OrganizationInvitation extends BaseEntity {

    @NotNull(message = "Organization context is required.")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false, foreignKey = @ForeignKey(name = "fk_organization_invitations_organization"))
    private Organization organization;

    @NotBlank(message = "Invitee email is required.")
    @Email(message = "Invalid email formatting structure.")
    @Size(max = 150, message = "Email must not exceed 150 characters.")
    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @NotBlank(message = "Invite token is required.")
    @Size(max = 100, message = "Token string must not exceed 100 characters.")
    @Column(name = "invite_token", nullable = false, length = 100)
    private String inviteToken;

    @NotNull(message = "Expiration timestamp is required.")
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "accepted", nullable = false)
    private boolean accepted = false;

    @NotNull(message = "Invitor user ID context is required.")
    @Column(name = "invited_by", nullable = false)
    private UUID invitedBy;
}
