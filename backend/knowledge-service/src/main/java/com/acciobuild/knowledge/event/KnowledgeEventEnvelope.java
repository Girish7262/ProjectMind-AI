package com.acciobuild.knowledge.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Standard serialization envelope wrapping event payload along with metadata and headers.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeEventEnvelope {
    private KnowledgeEventMetadata metadata;
    private KnowledgeEventHeaders headers;
    private String payloadJson;
}
