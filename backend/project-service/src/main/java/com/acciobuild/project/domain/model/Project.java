package com.acciobuild.project.domain.model;

import com.acciobuild.project.enums.ProjectStatus;
import com.acciobuild.project.enums.ProjectVisibility;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * JPA Entity representing the Project aggregate root.
 * Enforces organization scopes and cascades metadata limits settings.
 */
@Entity
@Table(name = "projects", uniqueConstraints = {
        @UniqueConstraint(name = "uq_project_code", columnNames = {"project_code"}),
        @UniqueConstraint(name = "uq_project_name_org", columnNames = {"organization_id", "project_name"})
})
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = java.util.UUID.class))
@Filter(name = "tenantFilter", condition = "organization_id = :tenantId")
@Getter
@Setter
public class Project {

    @Id
    @Column(name = "id")
    private UUID id;

    @NotNull(message = "Organization boundary association is required.")
    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @NotBlank(message = "Project code identifier is required.")
    @Size(max = 50)
    @Column(name = "project_code", nullable = false, length = 50)
    private String projectCode;

    @NotBlank(message = "Project name identifier is required.")
    @Size(max = 100)
    @Column(name = "project_name", nullable = false, length = 100)
    private String projectName;

    @Size(max = 100)
    @Column(name = "display_name", length = 100)
    private String displayName;

    @Size(max = 500)
    @Column(name = "description", length = 500)
    private String description;

    @NotNull(message = "Project status is required.")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ProjectStatus status = ProjectStatus.PLANNING;

    @NotNull(message = "Project visibility is required.")
    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false, length = 20)
    private ProjectVisibility visibility = ProjectVisibility.PRIVATE;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "updated_by", nullable = false)
    private UUID updatedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private ProjectSettings settings;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProjectMember> members = new HashSet<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProjectTag> tags = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
