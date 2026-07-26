package com.acciobuild.common.util;

import java.util.UUID;

/**
 * Custom ID generator ensuring consistent UUID instantiation across microservices.
 */
public final class IdGenerator {
    private IdGenerator() {}

    /**
     * Generates a new random UUID v4.
     */
    public static UUID generateUuid() {
        return UUID.randomUUID();
    }

    /**
     * Parses a string representation into a UUID object, returning null on empty input.
     */
    public static UUID parseUuid(String uuidString) {
        if (uuidString == null || uuidString.trim().isEmpty()) {
            return null;
        }
        return UUID.fromString(uuidString);
    }
}
