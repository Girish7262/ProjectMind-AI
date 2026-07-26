package com.acciobuild.auth.entity;

import com.acciobuild.common.entity.AuditEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * JPA Entity mapping Roles definitions mapping set permissions collections.
 */
@Entity
@Table(
        name = "roles",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"name"}, name = "uk_roles_name")
        },
        indexes = {
                @Index(columnList = "name", name = "idx_roles_name")
        }
)
@Getter
@Setter
public class Role extends AuditEntity {

    @NotBlank(message = "Role name is required.")
    @Size(max = 50, message = "Role name must not exceed 50 characters.")
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Size(max = 255, message = "Description must not exceed 255 characters.")
    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "status", nullable = false)
    private boolean status = true;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id"),
            foreignKey = @ForeignKey(name = "fk_role_permissions_role"),
            inverseForeignKey = @ForeignKey(name = "fk_role_permissions_permission")
    )
    private Set<Permission> permissions = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return Objects.equals(name, role.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "Role{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                ", status=" + status +
                '}';
    }
}
