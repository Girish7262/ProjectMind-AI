package com.acciobuild.ai.prompt;

import com.acciobuild.ai.domain.model.AiConversation;
import com.acciobuild.ai.domain.model.AiConversationMemory;
import com.acciobuild.ai.domain.repository.AiConversationMemoryRepository;
import com.acciobuild.ai.domain.repository.AiConversationRepository;
import com.acciobuild.ai.security.AiUserDetails;
import com.acciobuild.ai.service.ContextBuilderService;
import com.acciobuild.ai.dto.ContextDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Resolver for dynamic placeholders inside prompt templates.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PromptVariableResolver {

    private final AiConversationRepository conversationRepository;
    private final AiConversationMemoryRepository memoryRepository;
    private final ContextBuilderService contextBuilderService;

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{([^}]+)\\}\\}");

    public String resolve(String template, Map<String, Object> customVariables, UUID conversationId) {
        if (template == null) {
            return "";
        }

        Optional<AiConversation> conversationOpt = conversationId != null ? 
                conversationRepository.findById(conversationId) : Optional.empty();

        Matcher matcher = VARIABLE_PATTERN.matcher(template);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            String variableName = matcher.group(1).trim();
            String resolvedValue = resolveVariable(variableName, customVariables, conversationOpt);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(resolvedValue));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    private String resolveVariable(String name, Map<String, Object> customVariables, Optional<AiConversation> conversationOpt) {
        if (customVariables != null && customVariables.containsKey(name) && customVariables.get(name) != null) {
            return customVariables.get(name).toString();
        }

        switch (name.toLowerCase()) {
            case "user":
                return getAuthenticatedUser();
            case "organization":
                return conversationOpt.map(c -> c.getOrganizationId().toString())
                        .orElseGet(() -> com.acciobuild.ai.multitenancy.TenantContext.getCurrentTenant() != null ? 
                                com.acciobuild.ai.multitenancy.TenantContext.getCurrentTenant().toString() : "System");
            case "project":
                return conversationOpt.map(c -> c.getProjectId().toString()).orElse("Unknown Project");
            case "conversation":
                return conversationOpt.map(c -> c.getId().toString()).orElse("Unknown Conversation");
            case "date":
                return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            case "language":
                return "English";
            case "timezone":
                return ZoneId.systemDefault().getId();
            case "memory":
                return conversationOpt.map(c -> getFormattedMemory(c.getId())).orElse("No Memory Context");
            case "context":
            case "knowledge":
                return conversationOpt.map(c -> getFormattedContext(c.getId())).orElse("No Knowledge Context");
            default:
                log.warn("Variable '{}' could not be resolved. Returning placeholder.", name);
                return "{?" + name + "?}";
        }
    }

    private String getAuthenticatedUser() {
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof AiUserDetails userDetails) {
                return userDetails.getUsername();
            }
        } catch (Exception e) {
            log.warn("Failed to retrieve authenticated user details: {}", e.getMessage());
        }
        return "AnonymousUser";
    }

    private String getFormattedMemory(UUID conversationId) {
        List<AiConversationMemory> memories = memoryRepository.findByConversationId(conversationId);
        if (memories.isEmpty()) {
            return "No prior conversation memories recorded.";
        }
        return memories.stream()
                .map(m -> String.format("- Scope[%s] Key[%s]: %s", m.getMemoryScope(), m.getMemoryKey(), m.getMemoryValue()))
                .collect(Collectors.joining("\n"));
    }

    private String getFormattedContext(UUID conversationId) {
        try {
            ContextDto context = contextBuilderService.getContextByConversation(conversationId);
            if (context == null || context.getSources() == null || context.getSources().isEmpty()) {
                return "No referenced knowledge sources.";
            }
            return context.getSources().stream()
                    .map(src -> String.format("[Source: %s, Score: %.2f] %s", src.getSourceType(), src.getScore(), src.getContent()))
                    .collect(Collectors.joining("\n\n"));
        } catch (Exception e) {
            log.warn("Could not retrieve context for prompt variables resolution: {}", e.getMessage());
            return "No referenced knowledge sources.";
        }
    }
}
