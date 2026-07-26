package com.acciobuild.auth.security.filter;

import com.acciobuild.auth.security.jwt.JwtTokenProvider;
import com.acciobuild.auth.security.jwt.JwtValidator;
import com.acciobuild.common.constant.HeaderConstants;
import com.acciobuild.common.constant.SecurityConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

/**
 * Request filter intercepting every protected endpoint query, extracting and validating Bearer JWTs,
 * and populating the Spring Security Context on success.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final JwtValidator tokenValidator;
    private final UserDetailsService userDetailsService;

    // List of public endpoints to skip filtering
    private static final List<String> PUBLIC_URLS = List.of(
            "/api/v1/auth/register",
            "/api/v1/auth/login",
            "/api/v1/auth/refresh",
            "/api/v1/auth/forgot-password",
            "/api/v1/auth/reset-password",
            "/v3/api-docs",
            "/swagger-ui",
            "/actuator/health",
            "/actuator/prometheus"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return PUBLIC_URLS.stream().anyMatch(path::startsWith);
    }

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
            if (tokenValidator.isValid(jwt)) {
                String email = tokenProvider.extractUsername(jwt);
                
                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                    
                    if (userDetails.isEnabled() && userDetails.isAccountNonLocked()) {
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        
                        // Inject correlation ID if present in request headers
                        String correlationId = request.getHeader(HeaderConstants.CORRELATION_ID);
                        if (correlationId != null) {
                            request.setAttribute("correlationId", correlationId);
                        }

                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        log.info("Successfully authenticated user: {}", email);
                    } else {
                        log.warn("Authentication failed: User account is disabled or locked");
                    }
                }
            } else {
                log.warn("Authentication failed: JWT signature or structure is invalid");
            }
        } catch (Exception e) {
            log.error("Failed to set user authentication context: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
