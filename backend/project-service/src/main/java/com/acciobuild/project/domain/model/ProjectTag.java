package com.acciobuild.project.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import java.util.UUID;

/**
 * JPA Entity mapping tag definitions applied to organization projects.
 */
@Entity
@Table(name = "project_tags", uniqueConstraints = {
        @UniqueConstraint(name = "uq_project_tag_name", columnNames = {"project_id", "tag_name"})
})
@Filter(name = "tenantFilter", condition = "project_id IN (SELECT p.id FROM projects p WHERE p.organization_id = :tenantId)")
@Getter
@Setter
public class ProjectTag {

    @Id
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false, foreignKey = @ForeignKey(name = "fk_project_tag_project"))
    private Project project;

    @NotBlank(message = "Tag name is required.")
    @Size(max = 50)
    @Column(name = "tag_name", nullable = false, length = 50)
    private String tagName;

    @Size(max = 10)
    @Column(name = "color", length = 10)
    private String color = "#6366f1";
}
