package com.acciobuild.organization.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO containing details to create or update an organization.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payload details to create or update an organization profile.")
public class OrganizationRequest {

    @NotBlank(message = "Organization code is required.")
    @Size(min = 3, max = 50, message = "Organization code must be between 3 and 50 characters.")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Organization code must contain only lowercase alphanumeric characters and hyphens.")
    @Schema(description = "Unique code slug of the organization.", example = "accio-corp", required = true)
    private String organizationCode;

    @NotBlank(message = "Organization name is required.")
    @Size(min = 2, max = 100, message = "Organization name must be between 2 and 100 characters.")
    @Schema(description = "Unique name of the organization.", example = "Accio Corporation", required = true)
    private String organizationName;

    @NotBlank(message = "Display name is required.")
    @Size(max = 150, message = "Display name must not exceed 150 characters.")
    @Schema(description = "Display/Friendly name of the organization.", example = "Accio Corp", required = true)
    private String displayName;

    @Size(max = 500, message = "Description must not exceed 500 characters.")
    @Schema(description = "Description of organization profile.", example = "SaaS enterprise services segment.")
    private String description;

    @Size(max = 255, message = "Logo URL must not exceed 255 characters.")
    @Schema(description = "Logo image URL link.", example = "http://assets.acciobuild.com/logos/accio-corp.png")
    private String logoUrl;

    @Size(max = 255, message = "Website must not exceed 255 characters.")
    @Pattern(regexp = "^(https?://)?([\\da-z.-]+)\\.([a-z.]{2,6})([/\\w .-]*)*/?$", message = "Please provide a valid website URL.")
    @Schema(description = "Website link URL.", example = "https://www.acciocorporation.com")
    private String website;

    @Size(max = 100, message = "Industry must not exceed 100 characters.")
    @Schema(description = "Domain industry segment.", example = "Artificial Intelligence")
    private String industry;

    @Size(max = 50, message = "Organization size must not exceed 50 characters.")
    @Schema(description = "Size band of employees.", example = "100-500")
    private String organizationSize;

    @NotBlank(message = "Country location is required.")
    @Size(max = 100, message = "Country must not exceed 100 characters.")
    @Schema(description = "HQ country location.", example = "United States", required = true)
    private String country;

    @NotBlank(message = "Timezone location is required.")
    @Size(max = 100, message = "Timezone must not exceed 100 characters.")
    @Schema(description = "Operational timezone value.", example = "America/New_York", required = true)
    private String timezone;
}
