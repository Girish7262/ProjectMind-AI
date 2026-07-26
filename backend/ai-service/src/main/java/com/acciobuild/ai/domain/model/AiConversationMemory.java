package com.acciobuild.ai.domain.model;

import com.acciobuild.ai.enums.MemoryScope;
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
 * Entity storing short-term summary context associated with a conversation session.
 */
@Entity
@Table(name = "ai_conversation_memories")
@Filter(name = "tenantFilter", condition = "organization_id = :tenantId")
@Getter
@Setter
public class AiConversationMemory implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "conversation_id", nullable = false)
    private UUID conversationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "memory_scope", nullable = false)
    private MemoryScope memoryScope;

    @Column(name = "memory_key", nullable = false)
    private String memoryKey;

    @Column(name = "memory_value", length = 8000)
    private String memoryValue;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
