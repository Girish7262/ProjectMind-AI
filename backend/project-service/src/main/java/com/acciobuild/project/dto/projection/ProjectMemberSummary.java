package com.acciobuild.project.dto.projection;

import java.util.UUID;

/**
 * Lightweight Spring Data JPA projection for ProjectMember queries.
 */
public interface ProjectMemberSummary {

    /**
     * Gets mapping ID.
     */
    UUID getId();

    /**
     * Gets collaborator user ID.
     */
    UUID getUserId();

    /**
     * Gets project access role.
     */
    String getRole();

    /**
     * Gets membership status.
     */
    String getStatus();
}
