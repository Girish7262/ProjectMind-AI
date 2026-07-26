package com.acciobuild.auth.service;

import com.acciobuild.auth.dto.AuditEventResponse;
import com.acciobuild.auth.entity.AuditEvent;
import com.acciobuild.auth.enums.AuditEventStatus;
import com.acciobuild.auth.enums.AuditEventType;
import com.acciobuild.auth.repository.AuditEventRepository;
import com.acciobuild.auth.service.impl.AuditEventServiceImpl;
import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.common.dto.PagedResponse;
import com.acciobuild.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests validating AuditEvent database queries, pagination mappings, and services.
 */
@ExtendWith(MockitoExtension.class)
public class AuditEventServiceTest {

    @Mock private AuditEventRepository auditEventRepository;

    @InjectMocks
    private AuditEventServiceImpl auditEventService;

    private AuditEvent sampleEvent;
    private UUID eventId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        eventId = UUID.randomUUID();
        userId = UUID.randomUUID();

        sampleEvent = new AuditEvent();
        sampleEvent.setId(eventId);
        sampleEvent.setUserId(userId);
        sampleEvent.setUsername("admin@acciobuild.com");
        sampleEvent.setEventType(AuditEventType.LOGIN_SUCCESS);
        sampleEvent.setEventStatus(AuditEventStatus.SUCCESS);
        sampleEvent.setTimestamp(LocalDateTime.now());
        sampleEvent.setIpAddress("127.0.0.1");
        sampleEvent.setEndpoint("/api/v1/auth/login");
    }

    @Test
    void testSaveEvent_Success() {
        auditEventService.saveEvent(sampleEvent);
        verify(auditEventRepository).save(sampleEvent);
    }

    @Test
    void testGetEventById_Success() {
        when(auditEventRepository.findById(eventId)).thenReturn(Optional.of(sampleEvent));

        ApiResponse<AuditEventResponse> response = auditEventService.getEventById(eventId);

        assertNotNull(response);
        assertEquals(200, response.getStatus());
        assertEquals("admin@acciobuild.com", response.getData().getUsername());
        assertEquals("LOGIN_SUCCESS", response.getData().getEventType());
    }

    @Test
    void testGetEventById_NotFound() {
        when(auditEventRepository.findById(eventId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            auditEventService.getEventById(eventId);
        });
    }

    @Test
    void testGetEvents_Search() {
        Page<AuditEvent> pageResult = new PageImpl<>(Collections.singletonList(sampleEvent), PageRequest.of(0, 20), 1);
        when(auditEventRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(pageResult);

        ApiResponse<PagedResponse<AuditEventResponse>> response = auditEventService.getEvents(
                userId, "admin@acciobuild.com", "LOGIN_SUCCESS", "SUCCESS", 
                0, 20, "timestamp", "DESC");

        assertNotNull(response);
        assertEquals(200, response.getStatus());
        assertEquals(1, response.getData().getContent().size());
        assertEquals("admin@acciobuild.com", response.getData().getContent().get(0).getUsername());
    }
}
