package com.acciobuild.auth.repository.projection;

import java.util.UUID;

/**
 * Projection interface returning user details without password hashes.
 */
public interface UserSummary {
    UUID getId();
    String getEmail();
    String getUsername();
    String getFirstName();
    String getLastName();
    String getStatus();
    UUID getOrganizationId();
}
