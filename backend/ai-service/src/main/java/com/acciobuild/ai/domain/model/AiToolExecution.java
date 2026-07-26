package com.acciobuild.ai.domain.model;

import com.acciobuild.ai.enums.ToolExecutionStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity capturing log metadata of function tool executions.
 */
@Entity
@Table(name = "ai_tool_executions")
@Filter(name = "tenantFilter", condition = "organization_id = :tenantId")
@Getter
@Setter
public class AiToolExecution implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "message_id", nullable = false)
    private UUID messageId;

    @Column(name = "tool_name", nullable = false)
    private String toolName;

    @Column(name = "arguments_json", length = 4000)
    private String argumentsJson;

    @Column(name = "response_json", length = 4000)
    private String responseJson;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ToolExecutionStatus status;

    @Column(name = "execution_duration_ms")
    private Long executionDurationMs;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
