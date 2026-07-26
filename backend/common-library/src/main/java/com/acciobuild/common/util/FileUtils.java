package com.acciobuild.common.util;

/**
 * Reusable utility methods for file properties mapping.
 */
public final class FileUtils {
    private FileUtils() {}

    public static String getFileExtension(String filename) {
        if (filename == null) return null;
        int lastIndex = filename.lastIndexOf('.');
        return lastIndex == -1 ? "" : filename.substring(lastIndex + 1).toLowerCase();
    }

    public static boolean isValidExtension(String filename, String... allowedExtensions) {
        String ext = getFileExtension(filename);
        if (ext == null) return false;
        for (String allowed : allowedExtensions) {
            if (ext.equalsIgnoreCase(allowed)) return true;
        }
        return false;
    }
}
