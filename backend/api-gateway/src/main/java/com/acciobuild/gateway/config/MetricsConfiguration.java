package com.acciobuild.gateway.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Micrometer configuration creating counter and timer metrics for requests, security checks, and rate limits.
 */
@Configuration
public class MetricsConfiguration {

    @Bean
    public Counter gatewayRequestsCounter(MeterRegistry registry) {
        return Counter.builder("gateway.requests.total")
                .description("Counts total requests entering the gateway reverse proxy")
                .register(registry);
    }

    @Bean
    public Counter jwtValidationsCounter(MeterRegistry registry) {
        return Counter.builder("gateway.security.jwt.validations")
                .description("Counts successful JWT validations")
                .register(registry);
    }

    @Bean
    public Counter authenticationFailuresCounter(MeterRegistry registry) {
        return Counter.builder("gateway.security.authentication.failures")
                .description("Counts total user authentication failures")
                .register(registry);
    }

    @Bean
    public Counter rateLimitViolationsCounter(MeterRegistry registry) {
        return Counter.builder("gateway.ratelimit.violations")
                .description("Counts API rate limit violations")
                .register(registry);
    }

    @Bean
    public Timer gatewayLatencyTimer(MeterRegistry registry) {
        return Timer.builder("gateway.request.latency")
                .description("Measures gateway round-trip latency")
                .register(registry);
    }
}
