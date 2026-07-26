package com.acciobuild.auth.entity;

import com.acciobuild.common.entity.AuditEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.util.Objects;

/**
 * JPA Entity mapping permission capabilities to secure REST endpoints access controls.
 */
@Entity
@Table(
        name = "permissions",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"name"}, name = "uk_permissions_name"),
                @UniqueConstraint(columnNames = {"code"}, name = "uk_permissions_code")
        },
        indexes = {
                @Index(columnList = "name", name = "idx_permissions_name"),
                @Index(columnList = "code", name = "idx_permissions_code")
        }
)
@Getter
@Setter
public class Permission extends AuditEntity {

    @NotBlank(message = "Permission name is required.")
    @Size(max = 100, message = "Permission name must not exceed 100 characters.")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Permission code is required.")
    @Size(max = 50, message = "Permission code must not exceed 50 characters.")
    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Size(max = 255, message = "Description must not exceed 255 characters.")
    @Column(name = "description", length = 255)
    private String description;

    @NotBlank(message = "Module is required.")
    @Size(max = 50, message = "Module name must not exceed 50 characters.")
    @Column(name = "module", nullable = false, length = 50)
    private String module;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Permission that = (Permission) o;
        return Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return "Permission{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", module='" + module + '\'' +
                '}';
    }
}
