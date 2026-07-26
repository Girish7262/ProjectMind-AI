package com.acciobuild.project.dto;

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
 * Data Transfer Object representing project settings limits.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO representing project settings limits.")
public class ProjectSettingsDto {

    @Schema(description = "Project ID associated with settings.")
    private UUID projectId;

    @Schema(description = "Enable AI capabilities for the project.")
    private boolean aiEnabled;

    @Schema(description = "Enable knowledge capture from project actions.")
    private boolean knowledgeCaptureEnabled;

    @Schema(description = "Enable static code analysis triggers.")
    private boolean codeAnalysisEnabled;

    @Schema(description = "Enable automated documentation builders.")
    private boolean documentationEnabled;

    @Min(value = 1, message = "Maximum repositories must be at least 1.")
    @Max(value = 50, message = "Maximum repositories must not exceed 50.")
    @Schema(description = "Max code repositories allowed to connect.", example = "5")
    private int maxRepositories;

    @NotBlank(message = "Default branch configuration is required.")
    @Size(max = 50, message = "Default branch name must not exceed 50 characters.")
    @Schema(description = "Default repository branch.", example = "main")
    private String defaultBranch;

    // Feature Flags
    private boolean repositorySyncEnabled;
    private boolean webhooksEnabled;
    private boolean ciCdEnabled;
    private boolean apiAccessEnabled;
    private boolean notificationsEnabled;
    private boolean auditLoggingEnabled;

    // Quotas
    private int maxDocuments;
    private int maxTeamMembers;
    private int storageLimitGb;
    private int dailyAiRequests;

    @Size(max = 250)
    private String webhookUrl;

    @Size(max = 100)
    private String allowedRepositoryProviders;

    @Size(max = 50)
    private String codeAnalysisProfile;

    @Size(max = 50)
    private String documentationTemplate;
}
