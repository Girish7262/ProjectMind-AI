package com.acciobuild.ai.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity mapping revisions of dynamic prompt configurations.
 */
@Entity
@Table(name = "ai_prompt_versions")
@Filter(name = "tenantFilter", condition = "organization_id = :tenantId")
@Getter
@Setter
public class AiPromptVersion implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private AiPromptTemplate template;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

    @Column(name = "system_instruction", length = 4000)
    private String systemInstruction;

    @Column(name = "user_template", length = 4000)
    private String userTemplate;

    @Column(name = "parameters_json", length = 2000)
    private String parametersJson;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
