package com.acciobuild.common.constant;

/**
 * Standard regular expression validation strings.
 */
public final class RegexConstants {
    private RegexConstants() {}

    // Requires minimum 8 characters, at least one uppercase letter, one lowercase letter, one number and one special character
    public static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
    
    // Standard email validator regex
    public static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";

    // Standard phone number regex (E.164 format)
    public static final String PHONE_REGEX = "^\\+[1-9]\\d{1,14}$";

    // Standard UUID v4 validation regex
    public static final String UUID_REGEX = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-4[0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$";
}
