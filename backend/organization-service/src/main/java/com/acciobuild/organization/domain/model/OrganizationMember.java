package com.acciobuild.organization.domain.model;

import com.acciobuild.common.entity.BaseEntity;
import com.acciobuild.organization.enums.MemberRole;
import com.acciobuild.organization.enums.MemberStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity representing the OrganizationMember aggregate root.
 * Applies Hibernate dynamic filtering for multi-tenant data isolation.
 */
@Entity
@Table(
        name = "organization_members",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"organization_id", "user_id"}, name = "uk_organization_members_org_user")
        },
        indexes = {
                @Index(columnList = "organization_id", name = "idx_organization_members_org"),
                @Index(columnList = "user_id", name = "idx_organization_members_user")
        }
)
@FilterDef(
        name = "tenantFilter",
        parameters = @ParamDef(name = "tenantId", type = UUID.class)
)
@Filter(name = "tenantFilter", condition = "organization_id = :tenantId")
@Getter
@Setter
public class OrganizationMember extends BaseEntity {

    @NotNull(message = "Organization is required.")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false, foreignKey = @ForeignKey(name = "fk_organization_members_organization"))
    private Organization organization;

    @NotNull(message = "User ID reference is required.")
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @NotNull(message = "Member role is required.")
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 30)
    private MemberRole role;

    @NotNull(message = "Joined timestamp is required.")
    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt = LocalDateTime.now();

    @NotNull(message = "Member status is required.")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MemberStatus status = MemberStatus.ACTIVE;
}
