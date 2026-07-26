package com.acciobuild.auth.controller;

import com.acciobuild.auth.dto.AuditEventResponse;
import com.acciobuild.auth.service.AuditEventService;
import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.common.dto.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * REST controller exposing endpoints to search, filter, and fetch platform security audit records.
 * Locked down to administrative roles only.
 */
@RestController
@RequestMapping("/api/v1/admin/audit")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Audit Log Console", description = "Endpoints providing administrative query access to security audits and tracing logs.")
public class AuditController {

    private final AuditEventService auditEventService;

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ORG_ADMIN')")
    @Operation(summary = "Search Security Audits", description = "Query all security audits utilizing dynamic filters (user ID, username, event type, status) with pagination and sorting support.")
    public ResponseEntity<ApiResponse<PagedResponse<AuditEventResponse>>> getAudits(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {

        log.info("REST request to search audit logs page. Filters: userId={}, username={}, type={}, status={}", 
                userId, username, eventType, status);
        
        ApiResponse<PagedResponse<AuditEventResponse>> response = auditEventService.getEvents(
                userId, username, eventType, status, page, size, sortBy, direction);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ORG_ADMIN')")
    @Operation(summary = "Fetch Audit Details", description = "Retrieve complete context metadata for a single audit event.")
    public ResponseEntity<ApiResponse<AuditEventResponse>> getAuditById(@PathVariable UUID id) {
        log.info("REST request to fetch audit details for ID: {}", id);
        ApiResponse<AuditEventResponse> response = auditEventService.getEventById(id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ORG_ADMIN')")
    @Operation(summary = "Filter Audits by User", description = "Paginates audits performed by a specific user profile.")
    public ResponseEntity<ApiResponse<PagedResponse<AuditEventResponse>>> getAuditsByUserId(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {

        log.info("REST request to fetch audits page for User ID: {}", userId);
        ApiResponse<PagedResponse<AuditEventResponse>> response = auditEventService.getEventsByUserId(
                userId, page, size, sortBy, direction);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/date-range")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ORG_ADMIN')")
    @Operation(summary = "Filter Audits by Date Range", description = "Retrieves all audit events logged between a specific start and end timestamp.")
    public ResponseEntity<ApiResponse<PagedResponse<AuditEventResponse>>> getAuditsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {

        log.info("REST request to fetch audits logged between {} and {}", startDate, endDate);
        ApiResponse<PagedResponse<AuditEventResponse>> response = auditEventService.getEventsByDateRange(
                startDate, endDate, page, size, sortBy, direction);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}
