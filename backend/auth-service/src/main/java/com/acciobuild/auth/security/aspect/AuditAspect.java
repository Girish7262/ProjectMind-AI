package com.acciobuild.auth.security.aspect;

import com.acciobuild.auth.entity.AuditEvent;
import com.acciobuild.auth.enums.AuditEventStatus;
import com.acciobuild.auth.enums.AuditEventType;
import com.acciobuild.auth.security.AuthUserDetails;
import com.acciobuild.auth.security.annotation.Audit;
import com.acciobuild.auth.security.event.SecurityEventPublisher;
import com.acciobuild.common.util.MdcHelper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Spring AOP Aspect capturing executions of methods decorated with @Audit.
 * Details method parameters, authentication properties, request context metadata, and publishes async events.
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private final SecurityEventPublisher eventPublisher;

    @Around("@annotation(audit)")
    public Object auditExecution(ProceedingJoinPoint joinPoint, Audit audit) throws Throwable {
        AuditEventType eventType = audit.value();
        log.debug("AOP Audit Aspect captured execution for: {}", eventType);

        LocalDateTime timestamp = LocalDateTime.now();
        Object result = null;
        Throwable throwable = null;

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable t) {
            throwable = t;
            throw t;
        } finally {
            try {
                buildAndPublishEvent(eventType, timestamp, throwable);
            } catch (Exception ex) {
                log.error("Failed to capture and publish audit event inside aspect: {}", ex.getMessage(), ex);
            }
        }
    }

    /**
     * Builds and publishes the audit event.
     */
    private void buildAndPublishEvent(AuditEventType eventType, LocalDateTime timestamp, Throwable throwable) {
        AuditEvent event = new AuditEvent();
        event.setEventType(eventType);
        event.setTimestamp(timestamp);

        // Resolve active user context
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            Object principal = auth.getPrincipal();
            if (principal instanceof AuthUserDetails) {
                AuthUserDetails details = (AuthUserDetails) principal;
                event.setUserId(details.getUser().getId());
                event.setUsername(details.getUser().getEmail());
                event.setOrganizationId(details.getUser().getOrganizationId());
            } else {
                event.setUsername(auth.getName());
            }
        } else {
            event.setUsername("Anonymous");
        }

        // Resolve request context
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

        // Resolve correlation details
        event.setCorrelationId(MdcHelper.getCorrelationId());

        // Resolve exception status details
        if (throwable != null) {
            event.setEventStatus(AuditEventStatus.FAILED);
            event.setFailureReason(truncate(throwable.getMessage(), 500));
        } else {
            event.setEventStatus(AuditEventStatus.SUCCESS);
        }

        eventPublisher.publishEvent(event);
    }

    private String truncate(String val, int maxLen) {
        if (val == null) return null;
        return val.length() > maxLen ? val.substring(0, maxLen - 3) + "..." : val;
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
