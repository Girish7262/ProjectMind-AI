package com.acciobuild.auth.service;

import com.acciobuild.auth.dto.AuditEventResponse;
import com.acciobuild.auth.entity.AuditEvent;
import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.common.dto.PagedResponse;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service interface outlining security audits event queries and log saving.
 */
public interface AuditEventService {

    /**
     * Commits a new security audit event record to the database.
     */
    void saveEvent(AuditEvent event);

    /**
     * Retrieves audit details mapped by specific ID.
     */
    ApiResponse<AuditEventResponse> getEventById(UUID id);

    /**
     * Searches and paginates audits based on filters like user ID, username, event type, and status.
     */
    ApiResponse<PagedResponse<AuditEventResponse>> getEvents(
            UUID userId, String username, String eventType, String status, 
            int page, int size, String sortBy, String direction);

    /**
     * Searches and paginates audits for a user.
     */
    ApiResponse<PagedResponse<AuditEventResponse>> getEventsByUserId(
            UUID userId, int page, int size, String sortBy, String direction);

    /**
     * Searches and paginates audits within a date-time range.
     */
    ApiResponse<PagedResponse<AuditEventResponse>> getEventsByDateRange(
            LocalDateTime startDate, LocalDateTime endDate, 
            int page, int size, String sortBy, String direction);
}
