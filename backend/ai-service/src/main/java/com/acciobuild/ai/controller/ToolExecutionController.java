package com.acciobuild.ai.controller;

import com.acciobuild.ai.dto.ToolDefinitionDto;
import com.acciobuild.ai.dto.ToolExecutionDto;
import com.acciobuild.ai.service.ToolExecutionService;
import com.acciobuild.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

/**
 * REST Controller exposing AI Agent Tools registration and execution log endpoints.
 */
@RestController
@RequestMapping("/api/v1/ai/tools")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Agent Tools", description = "AI Agent tools registration and execution log endpoints")
@PreAuthorize("isAuthenticated()")
public class ToolExecutionController {

    private final ToolExecutionService toolExecutionService;

    /**
     * Lists registered function calling schemas/tools available to agent systems.
     */
    @GetMapping
    @Operation(summary = "Get available tools", description = "Lists registered function calling schemas/tools available to agent systems")
    public ResponseEntity<ApiResponse<List<ToolDefinitionDto>>> getTools() {
        log.info("REST: Request to list available tools.");
        List<ToolDefinitionDto> list = toolExecutionService.getAvailableTools();
        return ResponseEntity.ok(new ApiResponse<>(200, "Agent tools retrieved successfully", list));
    }

    /**
     * Logs the parameter outputs and metrics durations of a tool run.
     */
    @PostMapping("/execute/log")
    @Operation(summary = "Log tool execution metrics", description = "Logs the parameter outputs and metrics durations of a tool run")
    public ResponseEntity<ApiResponse<ToolExecutionDto>> logExecution(@Valid @RequestBody ToolExecutionDto dto) {
        log.info("REST: Request to log tool run metrics for: {}", dto.getToolName());
        ToolExecutionDto result = toolExecutionService.logToolExecution(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(201, "Tool execution log saved successfully", result));
    }

    /**
     * Retrieves the parameters and error details of a specific tool execution.
     */
    @GetMapping("/execute/{executionId}")
    @Operation(summary = "Get tool execution log", description = "Retrieves the parameters and error details of a specific tool execution")
    public ResponseEntity<ApiResponse<ToolExecutionDto>> getExecutionLog(@PathVariable("executionId") UUID executionId) {
        log.info("REST: Request to get tool execution details: {}", executionId);
        ToolExecutionDto dto = toolExecutionService.getToolExecutionLog(executionId);
        return ResponseEntity.ok(new ApiResponse<>(200, "Tool run details retrieved", dto));
    }
}
