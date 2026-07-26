package com.acciobuild.ai.multitenancy;

import java.util.UUID;

/**
 * Thread-local context holder tracking the active tenant's organization identifier.
 * Ensures isolation boundaries across AI service actions.
 */
public final class TenantContext {

    private static final ThreadLocal<UUID> CURRENT_TENANT = new ThreadLocal<>();

    private TenantContext() {}

    /**
     * Binds the tenant organization ID to the current active execution thread.
     */
    public static void setCurrentTenant(UUID tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    /**
     * Retrieves the tenant organization ID associated with the current thread.
     */
    public static UUID getCurrentTenant() {
        return CURRENT_TENANT.get();
    }

    /**
     * Clears tenant context from the thread local context.
     */
    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
