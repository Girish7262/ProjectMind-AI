package com.acciobuild.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;

/**
 * Configuration bean enabling global method security integrations.
 * Registers custom role hierarchy matrices and permission evaluators.
 */
@Configuration
public class MethodSecurityConfig {

    /**
     * Define the corporate role hierarchy using standard string representations.
     */
    @Bean
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
        roleHierarchy.setHierarchy(
            "ROLE_SUPER_ADMIN > ROLE_ORG_ADMIN\n" +
            "ROLE_ORG_ADMIN > ROLE_PROJECT_ADMIN\n" +
            "ROLE_PROJECT_ADMIN > ROLE_DEVELOPER\n" +
            "ROLE_DEVELOPER > ROLE_REVIEWER\n" +
            "ROLE_REVIEWER > ROLE_VIEWER"
        );
        return roleHierarchy;
    }

    /**
     * Configures the expression handler to use our custom permission evaluator and role hierarchy.
     */
    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler(
            RoleHierarchy roleHierarchy, 
            PermissionEvaluator permissionEvaluator) {
        
        DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setRoleHierarchy(roleHierarchy);
        expressionHandler.setPermissionEvaluator(permissionEvaluator);
        return expressionHandler;
    }
}
