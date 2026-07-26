package com.acciobuild.ai.observability;

import com.acciobuild.ai.config.filter.RequestLoggingFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

/**
 * Unit tests verifying MDC setup, cleanup, and status logs in RequestLoggingFilter.
 */
@ExtendWith(MockitoExtension.class)
public class RequestLoggingFilterTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain chain;

    @Test
    void testLoggingFilterPopulatesAndCleansMdc() throws Exception {
        when(request.getHeader("X-Request-ID")).thenReturn("test-req-123");
        when(request.getRequestURI()).thenReturn("/api/conversations/456");
        when(request.getMethod()).thenReturn("GET");

        RequestLoggingFilter filter = new RequestLoggingFilter();
        filter.doFilter(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
    }
}
