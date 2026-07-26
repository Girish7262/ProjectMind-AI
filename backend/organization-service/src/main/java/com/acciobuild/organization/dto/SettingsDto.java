package com.acciobuild.organization.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

/**
 * DTO representing organization settings.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO representing organization settings details.")
public class SettingsDto {

    @Schema(description = "Organization ID associated with settings.")
    private UUID organizationId;

    @Schema(description = "Enable AI capabilities.")
    private boolean aiEnabled;

    @Schema(description = "Enable knowledge sharing across projects.")
    private boolean knowledgeSharingEnabled;

    @NotBlank(message = "Default visibility configuration is required.")
    @Size(max = 20)
    @Schema(description = "Default projects visibility (PUBLIC or PRIVATE).", example = "PRIVATE")
    private String defaultVisibility;

    @Min(value = 1, message = "Maximum projects must be at least 1.")
    @Max(value = 1000, message = "Maximum projects must not exceed 1000.")
    @Schema(description = "Max projects allowed for the organization.", example = "10")
    private int maxProjects;

    @Min(value = 1, message = "Maximum members must be at least 1.")
    @Max(value = 10000, message = "Maximum members must not exceed 10000.")
    @Schema(description = "Max members allowed for the organization.", example = "50")
    private int maxMembers;

    // Feature Flags
    private boolean knowledgeBaseEnabled;
    private boolean projectModuleEnabled;
    private boolean documentUploadEnabled;
    private boolean apiAccessEnabled;
    private boolean teamManagementEnabled;
    private boolean auditLogsEnabled;
    private boolean notificationsEnabled;

    // Operational Settings
    private int maxStorageGb;
    private int maxApiRequestsPerDay;
    private int allowedFileSize;
    
    @Size(max = 200)
    private String allowedFileTypes;

    @Size(max = 10)
    private String defaultLanguage;

    @Size(max = 100)
    private String defaultTimezone;
}
