package com.acciobuild.organization.domain.model;

import com.acciobuild.common.entity.AuditEntity;
import com.acciobuild.organization.enums.OrganizationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.util.HashSet;
import java.util.Set;

/**
 * JPA Entity representing the root Organization aggregate representing multi-tenant SaaS profiles.
 */
@Entity
@Table(
        name = "organizations",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"organization_code"}, name = "uk_organizations_code"),
                @UniqueConstraint(columnNames = {"organization_name"}, name = "uk_organizations_name")
        },
        indexes = {
                @Index(columnList = "organization_code", name = "idx_organizations_code"),
                @Index(columnList = "organization_name", name = "idx_organizations_name")
        }
)
@Getter
@Setter
public class Organization extends AuditEntity {

    @NotBlank(message = "Organization code is required.")
    @Size(max = 50, message = "Organization code must not exceed 50 characters.")
    @Column(name = "organization_code", nullable = false, length = 50)
    private String organizationCode;

    @NotBlank(message = "Organization name is required.")
    @Size(max = 100, message = "Organization name must not exceed 100 characters.")
    @Column(name = "organization_name", nullable = false, length = 100)
    private String organizationName;

    @NotBlank(message = "Display name is required.")
    @Size(max = 150, message = "Display name must not exceed 150 characters.")
    @Column(name = "display_name", nullable = false, length = 150)
    private String displayName;

    @Size(max = 500, message = "Description must not exceed 500 characters.")
    @Column(name = "description", length = 500)
    private String description;

    @Size(max = 255, message = "Logo URL must not exceed 255 characters.")
    @Column(name = "logo_url", length = 255)
    private String logoUrl;

    @Size(max = 255, message = "Website URL must not exceed 255 characters.")
    @Column(name = "website", length = 255)
    private String website;

    @Size(max = 100, message = "Industry must not exceed 100 characters.")
    @Column(name = "industry", length = 100)
    private String industry;

    @Size(max = 50, message = "Organization size must not exceed 50 characters.")
    @Column(name = "organization_size", length = 50)
    private String organizationSize;

    @NotBlank(message = "Country is required.")
    @Size(max = 100, message = "Country must not exceed 100 characters.")
    @Column(name = "country", nullable = false, length = 100)
    private String country;

    @NotBlank(message = "Timezone is required.")
    @Size(max = 100, message = "Timezone must not exceed 100 characters.")
    @Column(name = "timezone", nullable = false, length = 100)
    private String timezone;

    @NotNull(message = "Organization status is required.")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrganizationStatus status = OrganizationStatus.ACTIVE;

    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<OrganizationMember> members = new HashSet<>();

    @OneToOne(mappedBy = "organization", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private OrganizationSettings settings;
}
