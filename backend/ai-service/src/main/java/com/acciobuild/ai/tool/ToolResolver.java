package com.acciobuild.ai.tool;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Resolver locating matching registered tools by name keys.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ToolResolver {

    private final ToolRegistry registry;

    public Optional<Tool> resolve(String name) {
        log.debug("Resolving tool by name: {}", name);
        return Optional.ofNullable(registry.getTool(name));
    }
}
