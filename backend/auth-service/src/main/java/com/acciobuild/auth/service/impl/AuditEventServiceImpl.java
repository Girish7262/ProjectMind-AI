package com.acciobuild.auth.service.impl;

import com.acciobuild.auth.dto.AuditEventResponse;
import com.acciobuild.auth.entity.AuditEvent;
import com.acciobuild.auth.enums.AuditEventStatus;
import com.acciobuild.auth.enums.AuditEventType;
import com.acciobuild.auth.repository.AuditEventRepository;
import com.acciobuild.auth.service.AuditEventService;
import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.common.dto.PagedResponse;
import com.acciobuild.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service implementation managing AuditEvent database operations and dynamic inquiries.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditEventServiceImpl implements AuditEventService {

    private final AuditEventRepository auditEventRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveEvent(AuditEvent event) {
        log.info("Saving audit event to DB: {} | User: {}", event.getEventType(), event.getUsername());
        auditEventRepository.save(event);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<AuditEventResponse> getEventById(UUID id) {
        log.info("Querying audit event by ID: {}", id);
        AuditEvent event = auditEventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Audit event not found.", "AUDIT_NOT_FOUND"));
        
        return ApiResponse.<AuditEventResponse>builder()
                .status(200)
                .message("Audit event details fetched.")
                .data(mapToResponse(event))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<PagedResponse<AuditEventResponse>> getEvents(
            UUID userId, String username, String eventType, String status, 
            int page, int size, String sortBy, String direction) {
        
        log.info("Querying audit events: userId={}, username={}, type={}, status={}", 
                userId, username, eventType, status);

        Sort.Direction dir = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sortBy));

        Specification<AuditEvent> spec = Specification.where(null);

        if (userId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("userId"), userId));
        }
        if (username != null && !username.trim().isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("username")), "%" + username.toLowerCase() + "%"));
        }
        if (eventType != null && !eventType.trim().isEmpty()) {
            try {
                AuditEventType typeEnum = AuditEventType.valueOf(eventType.toUpperCase().trim());
                spec = spec.and((root, query, cb) -> cb.equal(root.get("eventType"), typeEnum));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid event type filter provided: {}", eventType);
            }
        }
        if (status != null && !status.trim().isEmpty()) {
            try {
                AuditEventStatus statusEnum = AuditEventStatus.valueOf(status.toUpperCase().trim());
                spec = spec.and((root, query, cb) -> cb.equal(root.get("eventStatus"), statusEnum));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid status filter provided: {}", status);
            }
        }

        Page<AuditEvent> pageResult = auditEventRepository.findAll(spec, pageable);
        return ApiResponse.<PagedResponse<AuditEventResponse>>builder()
                .status(200)
                .message("Audit events page fetched successfully.")
                .data(mapToPagedResponse(pageResult))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<PagedResponse<AuditEventResponse>> getEventsByUserId(
            UUID userId, int page, int size, String sortBy, String direction) {
        
        log.info("Querying audit events for user ID: {}", userId);
        Sort.Direction dir = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sortBy));
        
        Page<AuditEvent> pageResult = auditEventRepository.findByUserId(userId, pageable);
        return ApiResponse.<PagedResponse<AuditEventResponse>>builder()
                .status(200)
                .message("User audit events fetched.")
                .data(mapToPagedResponse(pageResult))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<PagedResponse<AuditEventResponse>> getEventsByDateRange(
            LocalDateTime startDate, LocalDateTime endDate, 
            int page, int size, String sortBy, String direction) {
        
        log.info("Querying audit events from {} to {}", startDate, endDate);
        Sort.Direction dir = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sortBy));
        
        Page<AuditEvent> pageResult = auditEventRepository.findByDateRange(startDate, endDate, pageable);
        return ApiResponse.<PagedResponse<AuditEventResponse>>builder()
                .status(200)
                .message("Date range audit events fetched.")
                .data(mapToPagedResponse(pageResult))
                .build();
    }

    /**
     * Helper to map Spring Page to Custom PagedResponse DTO.
     */
    private PagedResponse<AuditEventResponse> mapToPagedResponse(Page<AuditEvent> page) {
        List<AuditEventResponse> content = page.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        
        return PagedResponse.<AuditEventResponse>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    /**
     * Helper mapping entity to DTO.
     */
    private AuditEventResponse mapToResponse(AuditEvent e) {
        if (e == null) return null;
        return AuditEventResponse.builder()
                .id(e.getId())
                .userId(e.getUserId())
                .username(e.getUsername())
                .organizationId(e.getOrganizationId())
                .sessionId(e.getSessionId())
                .ipAddress(e.getIpAddress())
                .browser(e.getBrowser())
                .device(e.getDevice())
                .operatingSystem(e.getOperatingSystem())
                .endpoint(e.getEndpoint())
                .httpMethod(e.getHttpMethod())
                .timestamp(e.getTimestamp())
                .eventType(e.getEventType().name())
                .eventStatus(e.getEventStatus().name())
                .failureReason(e.getFailureReason())
                .correlationId(e.getCorrelationId())
                .build();
    }
}
