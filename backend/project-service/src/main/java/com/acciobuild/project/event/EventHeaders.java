package com.acciobuild.project.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Tracing and tracking header tags passed with event envelopes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventHeaders {
    private String correlationId;
    private String traceId;
    private String triggeredBy;
}
