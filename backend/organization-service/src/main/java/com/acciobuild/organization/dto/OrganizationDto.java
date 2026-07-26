package com.acciobuild.organization.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO representing organization profile details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO representing organization details.")
public class OrganizationDto {

    @Schema(description = "Unique Identifier of the organization.")
    private UUID id;

    @Schema(description = "Code identifier of the organization (slug).")
    private String organizationCode;

    @Schema(description = "Unique name of the organization.")
    private String organizationName;

    @Schema(description = "Display name of the organization.")
    private String displayName;

    @Schema(description = "Description of organization profile.")
    private String description;

    @Schema(description = "Logo image URL.")
    private String logoUrl;

    @Schema(description = "Website link.")
    private String website;

    @Schema(description = "Domain industry segment.")
    private String industry;

    @Schema(description = "Size band (e.g. 10-50, 100-500).")
    private String organizationSize;

    @Schema(description = "HQ country location.")
    private String country;

    @Schema(description = "Active timezone.")
    private String timezone;

    @Schema(description = "Status state (e.g. ACTIVE, SUSPENDED).")
    private String status;

    @Schema(description = "Creation date timestamp.")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp.")
    private LocalDateTime updatedAt;
}
