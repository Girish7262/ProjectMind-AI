package com.acciobuild.project.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Standard envelope wrapping serialized event payloads along with tracking metadata
 * and tracing headers. Ready for Kafka and generic messaging platforms.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventEnvelope {
    private EventMetadata metadata;
    private EventHeaders headers;
    private String payload;
}
