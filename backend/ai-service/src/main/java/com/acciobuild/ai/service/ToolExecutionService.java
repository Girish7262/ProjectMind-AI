package com.acciobuild.ai.service;

import com.acciobuild.ai.dto.ToolDefinitionDto;
import com.acciobuild.ai.dto.ToolExecutionDto;
import java.util.List;
import java.util.UUID;

/**
 * Service Contract for managing schema metadata and run metrics log records of tool calls.
 */
public interface ToolExecutionService {
    List<ToolDefinitionDto> getAvailableTools();
    ToolExecutionDto logToolExecution(ToolExecutionDto executionDto);
    ToolExecutionDto getToolExecutionLog(UUID id);
}
