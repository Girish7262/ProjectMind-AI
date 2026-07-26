package com.acciobuild.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Standard sorting request specifications.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SortRequest {
    private String sortBy;
    private String direction; // "asc" or "desc"
}
