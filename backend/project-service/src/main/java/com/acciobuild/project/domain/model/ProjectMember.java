package com.acciobuild.project.domain.model;

import com.acciobuild.project.enums.ProjectMemberRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity mapping collaborator membership associations within a project.
 */
@Entity
@Table(name = "project_members", uniqueConstraints = {
        @UniqueConstraint(name = "uq_project_member_user", columnNames = {"project_id", "user_id"})
})
@Filter(name = "tenantFilter", condition = "project_id IN (SELECT p.id FROM projects p WHERE p.organization_id = :tenantId)")
@Getter
@Setter
public class ProjectMember {

    @Id
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false, foreignKey = @ForeignKey(name = "fk_project_member_project"))
    private Project project;

    @NotNull(message = "User ID reference is required.")
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @NotNull(message = "Member role is required.")
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private ProjectMemberRole role = ProjectMemberRole.DEVELOPER;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt = LocalDateTime.now();

    @Size(max = 20)
    @Column(name = "status", nullable = false, length = 20)
    private String status = "ACTIVE";
}
