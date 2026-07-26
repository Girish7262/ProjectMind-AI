package com.acciobuild.ai.config.filter;

import com.acciobuild.ai.multitenancy.TenantContext;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Filter generating structured logs populating execution metadata, tenant IDs, and latency details.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@Slf4j
public class RequestLoggingFilter implements Filter {

    private static final Pattern CONVERSATION_PATTERN = Pattern.compile("/conversations/([^/]+)");

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpRequest && response instanceof HttpServletResponse httpResponse) {
            String requestId = httpRequest.getHeader("X-Request-ID");
            if (requestId == null || httpRequest.getHeader("X-Request-ID") == null) {
                requestId = UUID.randomUUID().toString();
            }
            MDC.put("requestId", requestId);
            MDC.put("executionId", UUID.randomUUID().toString());

            UUID tenant = TenantContext.getCurrentTenant();
            if (tenant != null) {
                MDC.put("tenantId", tenant.toString());
                MDC.put("orgId", tenant.toString());
            }

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getName() != null) {
                MDC.put("userId", auth.getName());
            }

            String path = httpRequest.getRequestURI();
            Matcher matcher = CONVERSATION_PATTERN.matcher(path);
            if (matcher.find()) {
                MDC.put("conversationId", matcher.group(1));
            }

            long start = System.currentTimeMillis();
            MDC.put("timestamp", String.valueOf(start));

            log.info("Request started: {} {}", httpRequest.getMethod(), httpRequest.getRequestURI());

            try {
                chain.doFilter(request, response);
            } finally {
                long latency = System.currentTimeMillis() - start;
                MDC.put("latency", String.valueOf(latency));
                log.info("Request completed: status={}, latency={}ms", httpResponse.getStatus(), latency);

                MDC.remove("requestId");
                MDC.remove("executionId");
                MDC.remove("tenantId");
                MDC.remove("orgId");
                MDC.remove("userId");
                MDC.remove("conversationId");
                MDC.remove("timestamp");
                MDC.remove("latency");
            }
        } else {
            chain.doFilter(request, response);
        }
    }
}
