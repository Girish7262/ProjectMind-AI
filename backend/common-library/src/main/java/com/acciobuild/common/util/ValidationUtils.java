package com.acciobuild.common.util;

import com.acciobuild.common.constant.RegexConstants;
import java.util.regex.Pattern;

/**
 * Reusable utility methods for matching inputs against standard regex formatting.
 */
public final class ValidationUtils {
    private ValidationUtils() {}

    private static final Pattern EMAIL_PATTERN = Pattern.compile(RegexConstants.EMAIL_REGEX);
    private static final Pattern PHONE_PATTERN = Pattern.compile(RegexConstants.PHONE_REGEX);
    private static final Pattern UUID_PATTERN = Pattern.compile(RegexConstants.UUID_REGEX);

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    public static boolean isValidUuid(String uuid) {
        return uuid != null && UUID_PATTERN.matcher(uuid).matches();
    }
}
