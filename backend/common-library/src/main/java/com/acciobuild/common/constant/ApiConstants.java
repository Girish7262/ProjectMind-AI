package com.acciobuild.common.constant;

/**
 * Common REST API endpoint prefix paths.
 */
public final class ApiConstants {
    private ApiConstants() {}

    public static final String API_V1_PREFIX = "/api/v1";
    public static final String AUTH_API_BASE = API_V1_PREFIX + "/auth";
    public static final String ORG_API_BASE = API_V1_PREFIX + "/organizations";
    public static final String PROJECT_API_BASE = API_V1_PREFIX + "/projects";
    public static final String KNOWLEDGE_API_BASE = API_V1_PREFIX + "/knowledge";
    public static final String AI_API_BASE = API_V1_PREFIX + "/ai";
}
