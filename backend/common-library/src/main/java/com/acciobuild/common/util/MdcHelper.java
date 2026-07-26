package com.acciobuild.common.util;

import com.acciobuild.common.constant.HeaderConstants;
import org.slf4j.MDC;
import java.util.UUID;

/**
 * Helper class managing mapped diagnostic context (MDC) attributes, specifically tracing correlation IDs.
 */
public final class MdcHelper {
    private MdcHelper() {}

    /**
     * Initializes a correlation ID in MDC context, generating a new UUID if none exists.
     */
    public static void initCorrelationId(String existingId) {
        if (existingId == null || existingId.trim().isEmpty()) {
            MDC.put(HeaderConstants.CORRELATION_ID, UUID.randomUUID().toString());
        } else {
            MDC.put(HeaderConstants.CORRELATION_ID, existingId);
        }
    }

    /**
     * Retrieves the current correlation ID.
     */
    public static String getCorrelationId() {
        return MDC.get(HeaderConstants.CORRELATION_ID);
    }

    /**
     * Clears MDC parameters.
     */
    public static void clear() {
        MDC.clear();
    }
}
