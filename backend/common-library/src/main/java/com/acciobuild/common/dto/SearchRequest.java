package com.acciobuild.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * Standard request DTO mapping dynamic filters and search terms.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequest {
    private String query;
    private List<Filter> filters;
    private PaginationRequest pagination;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Filter {
        private String key;
        private String operator; // "eq", "like", "in", "gt", "lt"
        private Object value;
    }
}
