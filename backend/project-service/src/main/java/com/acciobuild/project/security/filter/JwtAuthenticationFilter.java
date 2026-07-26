package com.acciobuild.project.security.filter;

import com.acciobuild.common.constant.HeaderConstants;
import com.acciobuild.common.constant.SecurityConstants;
import com.acciobuild.common.security.JwtUtils;
import com.acciobuild.common.util.MdcHelper;
import com.acciobuild.project.multitenancy.TenantContext;
import com.acciobuild.project.security.ProjectUserDetails;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Stateless request filter validating Bearer JWTs, establishing SecurityContext,
 * and initializing multi-tenant database contexts for projects lookup.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader(SecurityConstants.JWT_HEADER);

        if (authHeader == null || !authHeader.startsWith(SecurityConstants.JWT_TOKEN_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(SecurityConstants.JWT_TOKEN_PREFIX.length());

        try {
            if (jwtUtils.validateToken(jwt)) {
                Claims claims = jwtUtils.parseClaims(jwt);

                String email = claims.getSubject();
                String userIdStr = claims.get(SecurityConstants.CLAIM_USER_ID, String.class);
                String orgIdStr = claims.get(SecurityConstants.CLAIM_ORGANIZATION_ID, String.class);

                // Read roles
                List<?> rawRoles = claims.get(SecurityConstants.CLAIM_ROLES, List.class);
                Collection<? extends GrantedAuthority> authorities = Collections.emptyList();
                if (rawRoles != null) {
                    authorities = rawRoles.stream()
                            .map(r -> new SimpleGrantedAuthority(r.toString()))
                            .collect(Collectors.toList());
                }

                if (email != null && userIdStr != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UUID userId = UUID.fromString(userIdStr);
                    UUID organizationId = (orgIdStr != null) ? UUID.fromString(orgIdStr) : null;

                    ProjectUserDetails principal = new ProjectUserDetails(userId, email, organizationId, authorities);

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            principal, null, authorities);
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("Statelessly authenticated user: {} (ID: {})", email, userId);

                    // Initialize tenant context boundary mapping
                    if (organizationId != null) {
                        TenantContext.setCurrentTenant(organizationId);
                    }

                    // Bind correlation ID for MDC tracing
                    String correlationId = request.getHeader(HeaderConstants.CORRELATION_ID);
                    if (correlationId != null) {
                        MdcHelper.initCorrelationId(correlationId);
                    }
                }
            } else {
                log.warn("Invalid JWT Signature. Request blocked.");
            }
        } catch (Exception e) {
            log.error("Failed to map stateless authentication contexts from JWT token: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            // Clean dynamic ThreadLocal variables to avoid cross-request context leaks
            TenantContext.clear();
            MdcHelper.clear();
        }
    }
}
