package com.acciobuild.common.enums;

/**
 * Standard system error code enumerations.
 */
public enum ErrorCode {
    RESOURCE_NOT_FOUND("ERR_404_001", "The requested resource could not be located."),
    UNAUTHORIZED_ACCESS("ERR_401_001", "Authentication credentials are invalid or missing."),
    FORBIDDEN_ACTION("ERR_403_001", "You lack permissions to perform this operation."),
    RESOURCE_CONFLICT("ERR_409_001", "A state conflict occurred. Resource already exists."),
    INPUT_VALIDATION_ERROR("ERR_400_001", "The request payload failed field validation rules."),
    BUSINESS_RULE_VIOLATION("ERR_400_002", "Operation violates corporate business rules."),
    INTERNAL_SERVER_ERROR("ERR_500_001", "An unexpected error occurred inside the server.");

    private final String code;
    private final String defaultMessage;

    ErrorCode(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public String getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
