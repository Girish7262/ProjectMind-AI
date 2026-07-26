package com.acciobuild.organization.multitenancy;

import java.util.UUID;

/**
 * Thread-local context holder tracking the active tenant's organization identifier.
 * Ensures isolation boundaries across service actions.
 */
public final class TenantContext {

    private static final ThreadLocal<UUID> CURRENT_TENANT = new ThreadLocal<>();

    private TenantContext() {}

    /**
     * Bind the tenant organization ID to the current active execution thread.
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
     * Crucial to call in MVC lifecycle filters to prevent thread-pool context leakage.
     */
    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
