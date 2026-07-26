package com.acciobuild.knowledge.config;

import com.acciobuild.common.security.JwtUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Separate configuration class defining the JwtUtils bean to break circular references
 * between SecurityConfig and JwtAuthenticationFilter.
 */
@Configuration
public class JwtConfig {

    @Value("${app.jwt.secret:dGhpcy1pcy1hLXNhZWZ0eS1zZWNyZXQta2V5LXdoaWNoLW11c3QtYmUtYXQtbGVhc3QtMjU2LWJpdHMtbG9uZy1mb3ItamN3dC1zaWduYXR1cmUtc2FmZXR5}")
    private String jwtSecret;

    @Bean
    public JwtUtils jwtUtils() {
        return new JwtUtils(jwtSecret);
    }
}
