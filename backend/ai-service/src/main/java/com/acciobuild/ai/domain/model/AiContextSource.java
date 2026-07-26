package com.acciobuild.ai.domain.model;

import com.acciobuild.ai.enums.ContextSourceType;
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
 * Entity mapping specific data references used in context assemblies.
 */
@Entity
@Table(name = "ai_context_sources")
@Filter(name = "tenantFilter", condition = "organization_id = :tenantId")
@Getter
@Setter
public class AiContextSource implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "context_id", nullable = false)
    private AiContext context;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false)
    private ContextSourceType sourceType;

    @Column(name = "source_id")
    private UUID sourceId;

    @Column(name = "content", length = 8000)
    private String content;

    @Column(name = "score")
    private Double score;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
