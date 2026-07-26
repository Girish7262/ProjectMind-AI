package com.acciobuild.common.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.UUID;

/**
 * Reusable utility extracting authenticated user details from SecurityContextHolder.
 */
public final class SecurityContextHelper {
    private SecurityContextHelper() {}

    /**
     * Retrieves the current active Authentication profile.
     */
    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * Checks if a user is currently authenticated.
     */
    public static boolean isAuthenticated() {
        Authentication auth = getAuthentication();
        return auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal());
    }

    /**
     * Extracts authenticated user ID from context credentials principal representation.
     */
    public static UUID getCurrentUserId() {
        if (!isAuthenticated()) {
            return null;
        }
        
        Authentication auth = getAuthentication();
        Object principal = auth.getPrincipal();
        
        if (principal != null) {
            // Use reflection to try and retrieve the user ID if the principal wraps a User object
            try {
                java.lang.reflect.Method getUserMethod = principal.getClass().getMethod("getUser");
                Object user = getUserMethod.invoke(principal);
                if (user != null) {
                    java.lang.reflect.Method getIdMethod = user.getClass().getMethod("getId");
                    Object idObj = getIdMethod.invoke(user);
                    if (idObj instanceof UUID) {
                        return (UUID) idObj;
                    }
                }
            } catch (Exception e) {
                // Silently fall back to other methods
            }
            
            // Try direct getId() on the principal itself
            try {
                java.lang.reflect.Method getIdMethod = principal.getClass().getMethod("getId");
                Object idObj = getIdMethod.invoke(principal);
                if (idObj instanceof UUID) {
                    return (UUID) idObj;
                }
            } catch (Exception e) {
                // Silently fall back
            }
        }
        
        try {
            // Fallback: assume the authentication name string is the UUID
            return UUID.fromString(auth.getName());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
