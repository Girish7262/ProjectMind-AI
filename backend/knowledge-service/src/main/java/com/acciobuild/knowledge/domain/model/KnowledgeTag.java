package com.acciobuild.knowledge.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import java.io.Serializable;
import java.util.UUID;

/**
 * JPA Entity representing customizable tags within a project.
 */
@Entity
@Table(name = "knowledge_tags")
@Filter(name = "tenantFilter", condition = "organization_id = :tenantId")
@Getter
@Setter
public class KnowledgeTag implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "color", length = 10)
    private String color = "#6366f1";
}
