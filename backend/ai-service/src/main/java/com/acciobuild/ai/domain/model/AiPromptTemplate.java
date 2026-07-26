package com.acciobuild.ai.domain.model;

import com.acciobuild.ai.enums.PromptStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Aggregate root representing dynamic templates for LLM system/user prompts.
 */
@Entity
@Table(name = "ai_prompt_templates", uniqueConstraints = {
        @UniqueConstraint(name = "uq_prompt_name", columnNames = {"name"})
})
@Filter(name = "tenantFilter", condition = "organization_id = :tenantId")
@Getter
@Setter
public class AiPromptTemplate implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PromptStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AiPromptVersion> versions = new ArrayList<>();
}
