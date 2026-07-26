package com.acciobuild.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Reusable utility methods for formatting and parsing date-time values.
 */
public final class DateUtils {
    private DateUtils() {}

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String formatIso(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.format(ISO_FORMATTER);
    }

    public static String formatDisplay(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.format(DISPLAY_FORMATTER);
    }

    public static LocalDateTime parseIso(String isoString) {
        return isoString == null ? null : LocalDateTime.parse(isoString, ISO_FORMATTER);
    }
}
