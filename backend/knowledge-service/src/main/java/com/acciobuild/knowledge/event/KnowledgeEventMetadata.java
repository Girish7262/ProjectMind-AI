package com.acciobuild.knowledge.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Common event metadata descriptors for Knowledge Service messaging.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeEventMetadata {
    private UUID id;
    private String aggregateId;
    private String aggregateType;
    private String eventType;
    private int eventVersion;
    private UUID tenantId;
    private UUID projectId;
    private LocalDateTime createdAt;
}
