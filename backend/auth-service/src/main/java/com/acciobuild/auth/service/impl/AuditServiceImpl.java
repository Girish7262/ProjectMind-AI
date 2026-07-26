package com.acciobuild.auth.service.impl;

import com.acciobuild.auth.entity.AuditEvent;
import com.acciobuild.auth.enums.AuditEventStatus;
import com.acciobuild.auth.enums.AuditEventType;
import com.acciobuild.auth.repository.UserRepository;
import com.acciobuild.auth.service.AuditService;
import com.acciobuild.auth.security.event.SecurityEventPublisher;
import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.common.util.MdcHelper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service implementation logging audits events.
 * Refactored to act as a bridge mapping legacy audits to the async SecurityEventPublisher.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditServiceImpl implements AuditService {

    private final SecurityEventPublisher eventPublisher;
    private final UserRepository userRepository;

    @Override
    public ApiResponse<Void> logEvent(UUID userId, String eventType, String severity, String message, UUID organizationId) {
        log.info("Bridge Audit Log - EventType: {} | UserId: {} | OrgId: {} | Message: {}", 
                eventType, userId, organizationId, message);

        try {
            AuditEvent event = new AuditEvent();
            event.setTimestamp(LocalDateTime.now());
            event.setUserId(userId);
            event.setOrganizationId(organizationId);
            event.setFailureReason(message);
            event.setCorrelationId(MdcHelper.getCorrelationId());
            
            // Map legacy event type string to the new AuditEventType enum
            event.setEventType(mapToEventType(eventType));
            
            // Map legacy severity string to AuditEventStatus enum
            event.setEventStatus(mapToStatus(severity));

            // Populate username if user ID is present
            if (userId != null) {
                userRepository.findById(userId).ifPresent(u -> event.setUsername(u.getEmail()));
            } else {
                event.setUsername("Anonymous");
            }

            // Resolve HTTP request context
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                event.setEndpoint(request.getRequestURI());
                event.setHttpMethod(request.getMethod());
                event.setIpAddress(request.getRemoteAddr());
                
                String userAgent = request.getHeader("User-Agent");
                event.setBrowser(getBrowser(userAgent));
                event.setDevice(getDevice(userAgent));
                event.setOperatingSystem(getOS(userAgent));
            }

            eventPublisher.publishEvent(event);
            
        } catch (Exception e) {
            log.error("Failed to map and publish legacy audit event: {}", e.getMessage(), e);
        }

        return ApiResponse.<Void>builder()
                .status(200)
                .message("Audit event bridged and queued successfully.")
                .build();
    }

    /**
     * Map legacy event string categories to standard types.
     */
    private AuditEventType mapToEventType(String legacyType) {
        if (legacyType == null) return AuditEventType.PROFILE_UPDATED;
        String type = legacyType.toUpperCase();
        
        if (type.contains("REGISTER")) return AuditEventType.REGISTER;
        if (type.contains("VERIFICATION_SUCCESS") || type.contains("VERIFIED")) return AuditEventType.EMAIL_VERIFIED;
        if (type.contains("LOGIN_SUCCESS") || type.equals("AUTH_LOGIN_SUCCESS")) return AuditEventType.LOGIN_SUCCESS;
        if (type.contains("LOGIN_FAILED") || type.contains("FAILED_LOGIN")) return AuditEventType.LOGIN_FAILED;
        if (type.contains("LOGOUT")) return AuditEventType.LOGOUT;
        if (type.contains("PASSWORD_RESET_REQUESTED")) return AuditEventType.PASSWORD_RESET;
        if (type.contains("PASSWORD_RESET_SUCCESS") || type.contains("PASSWORD_CHANGED")) return AuditEventType.PASSWORD_CHANGED;
        if (type.contains("ROLE_ASSIGNED")) return AuditEventType.ROLE_ASSIGNED;
        if (type.contains("ROLE_REMOVED")) return AuditEventType.ROLE_REMOVED;
        if (type.contains("PERMISSION_ADDED") || type.contains("PERMISSION_ASSIGNED")) return AuditEventType.PERMISSION_ASSIGNED;
        if (type.contains("PERMISSION_REMOVED")) return AuditEventType.PERMISSION_REMOVED;
        if (type.contains("REFRESH")) return AuditEventType.TOKEN_REFRESH;
        if (type.contains("LOCK")) return AuditEventType.ACCOUNT_LOCKED;
        if (type.contains("UNLOCK")) return AuditEventType.ACCOUNT_UNLOCKED;
        
        return AuditEventType.PROFILE_UPDATED;
    }

    /**
     * Map legacy severities (INFO, WARN, ERROR) to event outcome statuses.
     */
    private AuditEventStatus mapToStatus(String severity) {
        if (severity == null) return AuditEventStatus.INFO;
        String sev = severity.toUpperCase().trim();
        
        if (sev.equals("WARN")) return AuditEventStatus.WARNING;
        if (sev.equals("ERROR")) return AuditEventStatus.FAILED;
        if (sev.equals("INFO")) return AuditEventStatus.INFO;
        
        return AuditEventStatus.SUCCESS;
    }

    private String getDevice(String userAgent) {
        if (userAgent == null) return "Unknown Device";
        String ua = userAgent.toLowerCase();
        if (ua.contains("android") || ua.contains("iphone") || ua.contains("ipad")) {
            return "Mobile Device";
        }
        return "Desktop PC";
    }

    private String getBrowser(String userAgent) {
        if (userAgent == null) return "Unknown Browser";
        String ua = userAgent.toLowerCase();
        if (ua.contains("chrome")) return "Google Chrome";
        if (ua.contains("firefox")) return "Mozilla Firefox";
        if (ua.contains("safari") && !ua.contains("chrome")) return "Apple Safari";
        if (ua.contains("edge")) return "Microsoft Edge";
        return "Web Browser";
    }

    private String getOS(String userAgent) {
        if (userAgent == null) return "Unknown OS";
        String ua = userAgent.toLowerCase();
        if (ua.contains("windows")) return "Windows OS";
        if (ua.contains("macintosh") || ua.contains("mac os")) return "Mac OS";
        if (ua.contains("linux")) return "Linux OS";
        if (ua.contains("iphone") || ua.contains("ipad")) return "iOS";
        if (ua.contains("android")) return "Android OS";
        return "Generic OS";
    }
}
