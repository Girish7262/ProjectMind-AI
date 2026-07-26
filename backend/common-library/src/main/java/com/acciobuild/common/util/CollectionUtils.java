package com.acciobuild.common.util;

import java.util.Collection;

/**
 * Reusable utility methods for collection checking.
 */
public final class CollectionUtils {
    private CollectionUtils() {}

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }
}
