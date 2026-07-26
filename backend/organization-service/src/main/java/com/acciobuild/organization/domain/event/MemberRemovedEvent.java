package com.acciobuild.organization.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when a membership association is revoked.
 */
@Getter
public class MemberRemovedEvent extends OrganizationDomainEvent {

    private final UUID userId;

    /**
     * Constructs the event.
     */
    public MemberRemovedEvent(UUID tenantId, UUID userId, String correlationId) {
        super("MEMBER_REMOVED", tenantId, correlationId);
        this.userId = userId;
    }
}
