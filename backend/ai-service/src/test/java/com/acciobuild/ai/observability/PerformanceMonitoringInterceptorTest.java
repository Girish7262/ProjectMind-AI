package com.acciobuild.ai.observability;

import com.acciobuild.ai.config.interceptor.PerformanceMonitoringInterceptor;
import com.acciobuild.ai.domain.event.SlowRequestDetectedEvent;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests verifying latency checkpoints and slow request event publishing in PerformanceMonitoringInterceptor.
 */
@ExtendWith(MockitoExtension.class)
public class PerformanceMonitoringInterceptorTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;

    @Test
    void testInterceptorFiresSlowRequestEvent() {
        PerformanceMonitoringInterceptor interceptor = new PerformanceMonitoringInterceptor(eventPublisher);

        assertTrue(interceptor.preHandle(request, response, new Object()));

        long current = System.currentTimeMillis();
        when(request.getAttribute("startTime")).thenReturn(current - 1200);
        when(request.getRequestURI()).thenReturn("/api/query");
        when(request.getMethod()).thenReturn("POST");

        interceptor.afterCompletion(request, response, new Object(), null);

        verify(eventPublisher, times(1)).publishEvent(any(SlowRequestDetectedEvent.class));
    }
}
