package com.acciobuild.auth.security.interceptor;

import com.acciobuild.common.constant.HeaderConstants;
import com.acciobuild.common.util.MdcHelper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Spring MVC HandlerInterceptor capturing request context trace parameters.
 * Extracts correlation IDs from client headers and maps them to MDC logs contexts.
 */
@Component
public class AuditInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String correlationId = request.getHeader(HeaderConstants.CORRELATION_ID);
        // Fallback check standard headers if custom header constant not present
        if (correlationId == null || correlationId.trim().isEmpty()) {
            correlationId = request.getHeader("X-Correlation-Id");
        }
        
        MdcHelper.initCorrelationId(correlationId);
        
        // Propagate the correlation ID in the response headers for tracing
        response.setHeader(HeaderConstants.CORRELATION_ID, MdcHelper.getCorrelationId());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        MdcHelper.clear();
    }
}
