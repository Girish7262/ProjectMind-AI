package com.acciobuild.ai.observability;

import com.acciobuild.ai.config.filter.CorrelationIdFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests asserting MDC mapping and headers injection in CorrelationIdFilter.
 */
@ExtendWith(MockitoExtension.class)
public class CorrelationIdFilterTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain chain;

    @Test
    void testFilterInjectsCorrelationId() throws Exception {
        when(request.getHeader(CorrelationIdFilter.CORRELATION_HEADER)).thenReturn("");

        CorrelationIdFilter filter = new CorrelationIdFilter();
        filter.doFilter(request, response, chain);

        verify(response, times(1)).setHeader(eq(CorrelationIdFilter.CORRELATION_HEADER), anyString());
        verify(chain, times(1)).doFilter(request, response);
        assertNull(MDC.get(CorrelationIdFilter.CORRELATION_MDC_KEY));
    }
}
