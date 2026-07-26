package com.acciobuild.organization.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when a user is added as a member to an organization.
 */
@Getter
public class MemberAddedEvent extends OrganizationDomainEvent {

    private final UUID userId;
    private final String role;

    /**
     * Constructs the event.
     */
    public MemberAddedEvent(UUID tenantId, UUID userId, String role, String correlationId) {
        super("MEMBER_ADDED", tenantId, correlationId);
        this.userId = userId;
        this.role = role;
    }
}
