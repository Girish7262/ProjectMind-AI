package com.acciobuild.organization.domain.event;

import lombok.Getter;
import java.util.UUID;

/**
 * Domain event published when a new organization profile is registered.
 */
@Getter
public class OrganizationCreatedEvent extends OrganizationDomainEvent {

    private final String organizationCode;
    private final String organizationName;
    private final UUID creatorUserId;

    /**
     * Constructs the event.
     */
    public OrganizationCreatedEvent(UUID tenantId, String code, String name, UUID creator, String correlationId) {
        super("ORGANIZATION_CREATED", tenantId, correlationId);
        this.organizationCode = code;
        this.organizationName = name;
        this.creatorUserId = creator;
    }
}
