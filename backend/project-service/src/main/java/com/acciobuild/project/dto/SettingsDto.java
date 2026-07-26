package com.acciobuild.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

/**
 * Lightweight DTO mapping remote organization settings and limitations parameters.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettingsDto {
    private UUID organizationId;
    private int maxProjects;
    private int maxMembers;
    private boolean aiEnabled;
}
