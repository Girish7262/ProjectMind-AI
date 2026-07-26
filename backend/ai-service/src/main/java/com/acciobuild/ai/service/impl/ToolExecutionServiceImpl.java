package com.acciobuild.ai.service.impl;

import com.acciobuild.ai.domain.event.ToolExecutionRequestedEvent;
import com.acciobuild.ai.domain.model.AiToolDefinition;
import com.acciobuild.ai.domain.model.AiToolExecution;
import com.acciobuild.ai.domain.repository.AiToolDefinitionRepository;
import com.acciobuild.ai.domain.repository.AiToolExecutionRepository;
import com.acciobuild.ai.dto.ToolDefinitionDto;
import com.acciobuild.ai.dto.ToolExecutionDto;
import com.acciobuild.ai.exception.ToolExecutionException;
import com.acciobuild.ai.service.ToolExecutionService;
import com.acciobuild.ai.multitenancy.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service Implementation managing AI Agent tools registration and execution log records.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ToolExecutionServiceImpl implements ToolExecutionService {

    private final AiToolDefinitionRepository toolDefinitionRepository;
    private final AiToolExecutionRepository toolExecutionRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public List<ToolDefinitionDto> getAvailableTools() {
        log.info("Fetching available agent tools.");
        List<AiToolDefinition> list = toolDefinitionRepository.findAll();

        if (list.isEmpty()) {
            log.info("Initializing standard search tools inside the persistence store.");
            UUID orgId = TenantContext.getCurrentTenant() != null ? TenantContext.getCurrentTenant() : UUID.randomUUID();
            
            // Standard Tool 1: Knowledge Search
            AiToolDefinition t1 = new AiToolDefinition();
            t1.setId(UUID.randomUUID());
            t1.setOrganizationId(orgId);
            t1.setName("Knowledge Search");
            t1.setDescription("Queries semantic content index inside AccioBuild knowledge repository");
            t1.setParameterSchemaJson("{\"type\":\"object\",\"properties\":{\"query\":{\"type\":\"string\"}}}");
            t1.setIsActive(true);
            t1.setCreatedAt(LocalDateTime.now());
            toolDefinitionRepository.save(t1);
            list.add(t1);

            // Standard Tool 2: Project Search
            AiToolDefinition t2 = new AiToolDefinition();
            t2.setId(UUID.randomUUID());
            t2.setOrganizationId(orgId);
            t2.setName("Project Search");
            t2.setDescription("Search project metadata variables");
            t2.setParameterSchemaJson("{\"type\":\"object\",\"properties\":{\"projectId\":{\"type\":\"string\"}}}");
            t2.setIsActive(true);
            t2.setCreatedAt(LocalDateTime.now());
            toolDefinitionRepository.save(t2);
            list.add(t2);
        }

        return list.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ToolExecutionDto logToolExecution(ToolExecutionDto dto) {
        log.info("Logging tool execution for: {}", dto.getToolName());

        AiToolExecution exec = new AiToolExecution();
        exec.setId(UUID.randomUUID());
        exec.setOrganizationId(TenantContext.getCurrentTenant() != null ? TenantContext.getCurrentTenant() : UUID.randomUUID());
        exec.setMessageId(dto.getMessageId() != null ? dto.getMessageId() : UUID.randomUUID());
        exec.setToolName(dto.getToolName());
        exec.setArgumentsJson(dto.getArgumentsJson());
        exec.setResponseJson(dto.getResponseJson());
        exec.setStatus(dto.getStatus());
        exec.setExecutionDurationMs(dto.getExecutionDurationMs());
        exec.setErrorMessage(dto.getErrorMessage());
        exec.setCreatedAt(LocalDateTime.now());

        AiToolExecution saved = toolExecutionRepository.save(exec);

        eventPublisher.publishEvent(new ToolExecutionRequestedEvent(
                saved.getOrganizationId(),
                saved.getMessageId(),
                saved.getToolName(),
                UUID.randomUUID().toString()
        ));

        return mapToExecutionDto(saved);
    }

    @Override
    public ToolExecutionDto getToolExecutionLog(UUID id) {
        AiToolExecution exec = toolExecutionRepository.findById(id)
                .orElseThrow(() -> new ToolExecutionException("Tool execution log record not found: " + id));
        return mapToExecutionDto(exec);
    }

    private ToolDefinitionDto mapToDto(AiToolDefinition tool) {
        return ToolDefinitionDto.builder()
                .id(tool.getId())
                .name(tool.getName())
                .description(tool.getDescription())
                .parameterSchemaJson(tool.getParameterSchemaJson())
                .isActive(tool.getIsActive())
                .createdAt(tool.getCreatedAt())
                .build();
    }

    private ToolExecutionDto mapToExecutionDto(AiToolExecution exec) {
        return ToolExecutionDto.builder()
                .id(exec.getId())
                .messageId(exec.getMessageId())
                .toolName(exec.getToolName())
                .argumentsJson(exec.getArgumentsJson())
                .responseJson(exec.getResponseJson())
                .status(exec.getStatus())
                .executionDurationMs(exec.getExecutionDurationMs())
                .errorMessage(exec.getErrorMessage())
                .createdAt(exec.getCreatedAt())
                .build();
    }
}
