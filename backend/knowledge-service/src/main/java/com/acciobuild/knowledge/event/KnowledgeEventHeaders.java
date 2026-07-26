package com.acciobuild.knowledge.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Tracing headers attached to knowledge events.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeEventHeaders {
    private String correlationId;
    private String causationId;
    private String traceId;
    private String sourceService;
}
