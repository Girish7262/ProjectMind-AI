package com.acciobuild.ai.memory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolver evaluating and merging colliding keys in AI memory updates.
 */
@Component
@Slf4j
public class MemoryConflictResolver {

    public enum ConflictStrategy {
        OVERWRITE,
        APPEND,
        FAIL
    }

    public String resolveConflict(String existingValue, String newValue, ConflictStrategy strategy) {
        log.info("Resolving memory conflict using strategy: {}", strategy);
        if (existingValue == null) {
            return newValue;
        }
        if (newValue == null) {
            return existingValue;
        }

        switch (strategy) {
            case APPEND:
                return existingValue.strip() + " | " + newValue.strip();
            case FAIL:
                throw new IllegalStateException("Memory collision detected. Duplicate keys are forbidden under this retention context.");
            case OVERWRITE:
            default:
                return newValue;
        }
    }
}
