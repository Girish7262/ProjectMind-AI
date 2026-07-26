package com.acciobuild.auth.service;

import com.acciobuild.common.dto.ApiResponse;
import java.util.UUID;

/**
 * Service interface logging administrative audits events for compliance checks.
 */
public interface AuditService {

    ApiResponse<Void> logEvent(UUID userId, String eventType, String severity, String message, UUID organizationId);
}
