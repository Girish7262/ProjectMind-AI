package com.acciobuild.common.util;

import com.acciobuild.common.dto.PaginationRequest;
import com.acciobuild.common.dto.PaginationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Reusable utility parsing search parameters to database Pageable scopes.
 */
public final class PageUtils {
    private PageUtils() {}

    /**
     * Converts a custom PaginationRequest DTO to a Spring Pageable object.
     */
    public static Pageable toPageable(PaginationRequest request) {
        if (request == null) {
            return PageRequest.of(0, 20);
        }
        Sort.Direction direction = Sort.Direction.fromString(request.getDirection());
        return PageRequest.of(request.getPage(), request.getSize(), Sort.by(direction, request.getSortBy()));
    }

    /**
     * Extracts pagination metadata from a Spring Page results object.
     */
    public static PaginationResponse toPaginationResponse(Page<?> page) {
        if (page == null) {
            return null;
        }
        return PaginationResponse.builder()
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
