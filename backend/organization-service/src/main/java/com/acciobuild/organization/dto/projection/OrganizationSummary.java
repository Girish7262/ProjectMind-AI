package com.acciobuild.organization.dto.projection;

import java.util.UUID;

/**
 * Spring Data JPA Projection interface for lightweight organization details.
 * Prevents loading entire fields/CLOBs during bulk listing requests.
 */
public interface OrganizationSummary {

    /**
     * Retrieve organization unique identifier.
     */
    UUID getId();

    /**
     * Retrieve organization code (slug).
     */
    String getOrganizationCode();

    /**
     * Retrieve organization full unique name.
     */
    String getOrganizationName();

    /**
     * Retrieve organization display/friendly name.
     */
    String getDisplayName();

    /**
     * Retrieve operational status state.
     */
    String getStatus();

    /**
     * Retrieve HQ country location.
     */
    String getCountry();
}
