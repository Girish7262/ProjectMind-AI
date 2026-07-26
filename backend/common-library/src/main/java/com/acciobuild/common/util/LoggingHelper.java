package com.acciobuild.common.util;

import lombok.extern.slf4j.Slf4j;

/**
 * Reusable utility logging incoming HTTP request/response metrics.
 */
@Slf4j
public final class LoggingHelper {
    private LoggingHelper() {}

    public static void logRequest(String method, String uri, String queryParams, String correlationId) {
        log.info("Incoming request: {} {} | QueryParams: {} | Correlation ID: {}", 
                method, uri, queryParams == null ? "None" : queryParams, correlationId);
    }

    public static void logResponse(String method, String uri, int status, long durationMs, String correlationId) {
        log.info("Outgoing response: {} {} | Status: {} | Duration: {}ms | Correlation ID: {}", 
                method, uri, status, durationMs, correlationId);
    }
}
