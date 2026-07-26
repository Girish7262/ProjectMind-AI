package com.acciobuild.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import java.util.Map;

/**
 * Controller providing fallback API responses when downstream microservices trigger circuit breakers.
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @RequestMapping("/auth")
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Mono<Map<String, Object>> authFallback() {
        return Mono.just(Map.of("success", false, "message", "Authentication service is temporarily unavailable. Please try again later."));
    }

    @RequestMapping("/organization")
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Mono<Map<String, Object>> organizationFallback() {
        return Mono.just(Map.of("success", false, "message", "Organization service is temporarily unavailable. Please try again later."));
    }

    @RequestMapping("/project")
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Mono<Map<String, Object>> projectFallback() {
        return Mono.just(Map.of("success", false, "message", "Project service is temporarily unavailable. Please try again later."));
    }

    @RequestMapping("/knowledge")
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Mono<Map<String, Object>> knowledgeFallback() {
        return Mono.just(Map.of("success", false, "message", "Knowledge service is temporarily unavailable. Please try again later."));
    }

    @RequestMapping("/ai")
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Mono<Map<String, Object>> aiFallback() {
        return Mono.just(Map.of("success", false, "message", "AI service is temporarily unavailable. Please try again later."));
    }
}
