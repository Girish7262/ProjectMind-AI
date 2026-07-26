package com.acciobuild.ai.domain.model;

import com.acciobuild.ai.enums.CitationType;
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
 * Entity mapping reference source citations backing message generated responses.
 */
@Entity
@Table(name = "ai_citations")
@Filter(name = "tenantFilter", condition = "organization_id = :tenantId")
@Getter
@Setter
public class AiCitation implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    private AiConversationMessage message;

    @Enumerated(EnumType.STRING)
    @Column(name = "citation_type", nullable = false)
    private CitationType citationType;

    @Column(name = "source_id")
    private UUID sourceId;

    @Column(name = "title")
    private String title;

    @Column(name = "url")
    private String url;

    @Column(name = "snippet", length = 4000)
    private String snippet;

    @Column(name = "start_index")
    private Integer startIndex;

    @Column(name = "end_index")
    private Integer endIndex;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
