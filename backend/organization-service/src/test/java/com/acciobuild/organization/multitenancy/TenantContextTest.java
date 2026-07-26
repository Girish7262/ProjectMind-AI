package com.acciobuild.organization.multitenancy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying ThreadLocal tenant context isolation bounds.
 */
public class TenantContextTest {

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void testTenantIdBoundSuccessfully() {
        UUID tenantId = UUID.randomUUID();
        TenantContext.setCurrentTenant(tenantId);

        assertEquals(tenantId, TenantContext.getCurrentTenant());
    }

    @Test
    void testTenantContextCleared() {
        UUID tenantId = UUID.randomUUID();
        TenantContext.setCurrentTenant(tenantId);
        TenantContext.clear();

        assertNull(TenantContext.getCurrentTenant());
    }

    @Test
    void testThreadLocalIsolation() throws InterruptedException {
        UUID mainThreadTenant = UUID.randomUUID();
        TenantContext.setCurrentTenant(mainThreadTenant);

        Thread backgroundThread = new Thread(() -> {
            assertNull(TenantContext.getCurrentTenant());
            UUID bgThreadTenant = UUID.randomUUID();
            TenantContext.setCurrentTenant(bgThreadTenant);
            assertEquals(bgThreadTenant, TenantContext.getCurrentTenant());
            TenantContext.clear();
        });

        backgroundThread.start();
        backgroundThread.join();

        assertEquals(mainThreadTenant, TenantContext.getCurrentTenant());
    }
}
