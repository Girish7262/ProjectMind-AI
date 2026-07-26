package com.acciobuild.project.dto.projection;

import java.util.UUID;

/**
 * Lightweight Spring Data JPA projection for Project lists.
 * Prevents loading large text description fields to optimize database throughput.
 */
public interface ProjectSummary {

    /**
     * Gets project unique ID.
     */
    UUID getId();

    /**
     * Gets organization tenant ID.
     */
    UUID getOrganizationId();

    /**
     * Gets unique project code.
     */
    String getProjectCode();

    /**
     * Gets unique project name.
     */
    String getProjectName();

    /**
     * Gets display name.
     */
    String getDisplayName();

    /**
     * Gets project operational status.
     */
    String getStatus();

    /**
     * Gets visibility tier.
     */
    String getVisibility();
}
