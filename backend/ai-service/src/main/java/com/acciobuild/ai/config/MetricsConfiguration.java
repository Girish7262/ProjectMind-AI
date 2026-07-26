package com.acciobuild.ai.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Micrometer metrics configuration creating counters and timers beans.
 */
@Configuration
public class MetricsConfiguration {

    @Bean
    public Counter apiRequestsCounter(MeterRegistry registry) {
        return Counter.builder("acciobuild.api.requests")
                .description("Counts total API requests processed")
                .register(registry);
    }

    @Bean
    public Counter conversationCounter(MeterRegistry registry) {
        return Counter.builder("acciobuild.conversations.total")
                .description("Counts total conversation updates")
                .register(registry);
    }

    @Bean
    public Counter cacheHitsCounter(MeterRegistry registry) {
        return Counter.builder("acciobuild.cache.hits")
                .description("Counts cache hits")
                .register(registry);
    }

    @Bean
    public Counter cacheMissesCounter(MeterRegistry registry) {
        return Counter.builder("acciobuild.cache.misses")
                .description("Counts cache misses")
                .register(registry);
    }

    @Bean
    public Counter toolExecutionCounter(MeterRegistry registry) {
        return Counter.builder("acciobuild.tool.executions")
                .description("Counts tool executions")
                .register(registry);
    }

    @Bean
    public Counter retryCounter(MeterRegistry registry) {
        return Counter.builder("acciobuild.resilience.retries")
                .description("Counts recovery retry triggers")
                .register(registry);
    }

    @Bean
    public Counter timeoutCounter(MeterRegistry registry) {
        return Counter.builder("acciobuild.resilience.timeouts")
                .description("Counts timeout occurrences")
                .register(registry);
    }

    @Bean
    public Timer contextBuildTimer(MeterRegistry registry) {
        return Timer.builder("acciobuild.context.build.time")
                .description("Timer mapping context assembly duration")
                .register(registry);
    }
}
