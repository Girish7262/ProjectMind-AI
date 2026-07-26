package com.acciobuild.ai.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when a citation is attached to a generated response.
 */
@Getter
public class CitationAttachedEvent extends AiDomainEvent {
    private final UUID citationId;
    private final UUID messageId;

    public CitationAttachedEvent(UUID organizationId, UUID citationId, UUID messageId, String correlationId) {
        super("CITATION_ATTACHED", organizationId, correlationId);
        this.citationId = citationId;
        this.messageId = messageId;
    }
}
