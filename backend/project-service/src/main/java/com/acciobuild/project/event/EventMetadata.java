package com.acciobuild.project.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Common event metadata descriptors matching structural schemas.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventMetadata {
    private UUID eventId;
    private String eventType;
    private String aggregateType;
    private String aggregateId;
    private UUID organizationId;
    private UUID projectId;
    private LocalDateTime occurredAt;
    private int version;
}
