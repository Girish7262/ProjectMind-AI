package com.acciobuild.ai.config.interceptor;

import com.acciobuild.ai.domain.event.SlowRequestDetectedEvent;
import com.acciobuild.ai.multitenancy.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

/**
 * Interceptor mapping API endpoint request performance, publishing slow execution events.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PerformanceMonitoringInterceptor implements HandlerInterceptor {

    private final ApplicationEventPublisher eventPublisher;
    private static final long SLOW_THRESHOLD_MS = 1000L;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute("startTime", System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        Long startTime = (Long) request.getAttribute("startTime");
        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;
            if (duration > SLOW_THRESHOLD_MS) {
                log.warn("Slow API Request detected: {} {} took {}ms (threshold is {}ms)",
                        request.getMethod(), request.getRequestURI(), duration, SLOW_THRESHOLD_MS);

                UUID tenantId = TenantContext.getCurrentTenant();
                if (tenantId == null) {
                    tenantId = UUID.randomUUID();
                }
                String correlationId = response.getHeader("X-Correlation-ID");
                if (correlationId == null) {
                    correlationId = UUID.randomUUID().toString();
                }

                eventPublisher.publishEvent(new SlowRequestDetectedEvent(
                        tenantId,
                        request.getRequestURI(),
                        request.getMethod(),
                        duration,
                        correlationId
                ));
            }
        }
    }
}
