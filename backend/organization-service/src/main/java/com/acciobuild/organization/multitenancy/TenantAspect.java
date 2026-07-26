package com.acciobuild.organization.multitenancy;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Session;
import org.springframework.stereotype.Component;
import java.util.UUID;

/**
 * Aspect-oriented filter manager enforcing Hibernate tenant filters on data lookups.
 * Intercepts repository methods to bind active thread-local tenant parameters.
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class TenantAspect {

    private final EntityManager entityManager;

    /**
     * Intercepts repository method executions, unwrapping the Hibernate session
     * and enabling the dynamic tenantFilter parameter.
     */
    @Before("execution(* com.acciobuild.organization.domain.repository..*(..))")
    public void enableTenantFilter() {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId != null) {
            log.trace("AOP Tenant Aspect: Enabling tenant filter for ID: {}", tenantId);
            try {
                Session session = entityManager.unwrap(Session.class);
                if (session != null) {
                    session.enableFilter("tenantFilter")
                           .setParameter("tenantId", tenantId);
                }
            } catch (Exception e) {
                log.error("Failed to unwrap Hibernate session and enable tenant filter", e);
            }
        } else {
            log.trace("AOP Tenant Aspect: No tenant context set for current thread execution.");
        }
    }
}
