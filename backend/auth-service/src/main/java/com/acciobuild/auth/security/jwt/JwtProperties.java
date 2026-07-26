package com.acciobuild.auth.security.jwt;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties class mapping 'jwt' properties from application yml files.
 */
@Configuration
@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtProperties {
    private String secret;
    private long accessTokenExpiration = 900000; // 15 mins
    private long refreshTokenExpiration = 604800000; // 7 days
    private String issuer = "AccioBuild-IdP";
    private String audience = "AccioBuild-Client";
    private long clockSkew = 60; // 1 min allowed drift
}
