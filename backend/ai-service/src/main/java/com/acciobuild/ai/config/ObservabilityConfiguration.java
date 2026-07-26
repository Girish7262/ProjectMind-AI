package com.acciobuild.ai.config;

import com.acciobuild.ai.config.interceptor.PerformanceMonitoringInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Observability configuration binding handler performance interceptors.
 */
@Configuration
@RequiredArgsConstructor
public class ObservabilityConfiguration implements WebMvcConfigurer {

    private final PerformanceMonitoringInterceptor performanceInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(performanceInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/actuator/**", "/swagger-ui/**", "/v3/api-docs/**");
    }
}
