package com.acciobuild.common.dto;

import com.acciobuild.common.constant.ApplicationConstants;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Standard request DTO mapping pagination page configurations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginationRequest {

    @Min(value = 0, message = "Page index must not be negative.")
    @Builder.Default
    private int page = Integer.parseInt(ApplicationConstants.DEFAULT_PAGE_NUMBER);

    @Min(value = 1, message = "Page size must be at least 1.")
    @Max(value = 100, message = "Page size limit exceeded (Max 100).")
    @Builder.Default
    private int size = Integer.parseInt(ApplicationConstants.DEFAULT_PAGE_SIZE);

    @Builder.Default
    private String sortBy = ApplicationConstants.DEFAULT_SORT_BY;

    @Builder.Default
    private String direction = ApplicationConstants.DEFAULT_SORT_DIRECTION;
}
