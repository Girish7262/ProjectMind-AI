package com.acciobuild.ai.dto;

import com.acciobuild.ai.enums.ToolExecutionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object mapping invocation metrics and outputs of function tools.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolExecutionDto {
    private UUID id;
    private UUID organizationId;
    private UUID messageId;
    private String toolName;
    private String argumentsJson;
    private String responseJson;
    private ToolExecutionStatus status;
    private Long executionDurationMs;
    private String errorMessage;
    private LocalDateTime createdAt;
}
