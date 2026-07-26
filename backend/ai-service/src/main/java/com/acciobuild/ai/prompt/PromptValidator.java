package com.acciobuild.ai.prompt;

import com.acciobuild.ai.domain.model.AiPromptTemplate;
import com.acciobuild.ai.domain.model.AiPromptVersion;
import com.acciobuild.ai.exception.DuplicatePromptTemplateException;
import com.acciobuild.ai.multitenancy.TenantContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Validator class verifying structural syntax of placeholder tokens, name limits, and tenant configurations.
 */
@Component
public class PromptValidator {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{([^}]+)\\}\\}");
    private static final Set<String> BUILT_IN_VARIABLES = Set.of(
            "user", "organization", "project", "conversation", "knowledge", "date", "language", "timezone", "memory", "context"
    );

    public void validateTemplate(AiPromptTemplate template) {
        if (template == null) {
            throw new IllegalArgumentException("Prompt template cannot be null.");
        }
        if (!StringUtils.hasText(template.getName())) {
            throw new IllegalArgumentException("Prompt template name cannot be empty.");
        }
        validateTenant(template.getOrganizationId());
    }

    public void validateVersion(AiPromptVersion version) {
        if (version == null) {
            throw new IllegalArgumentException("Prompt version cannot be null.");
        }
        validateTenant(version.getOrganizationId());

        // Validate variables in both system instructions and user templates
        Set<String> variables = new HashSet<>();
        if (StringUtils.hasText(version.getSystemInstruction())) {
            variables.addAll(extractVariables(version.getSystemInstruction()));
        }
        if (StringUtils.hasText(version.getUserTemplate())) {
            variables.addAll(extractVariables(version.getUserTemplate()));
        }

        for (String var : variables) {
            if (!BUILT_IN_VARIABLES.contains(var)) {
                // Verify custom variable syntax
                if (!var.matches("^[a-zA-Z0-9_]+$")) {
                    throw new IllegalArgumentException("Invalid variable name syntax: '{" + var + "}'. Variables must be alphanumeric/underscores.");
                }
            }
        }
    }

    public Set<String> extractVariables(String content) {
        if (!StringUtils.hasText(content)) {
            return Collections.emptySet();
        }
        Set<String> variables = new HashSet<>();
        Matcher matcher = VARIABLE_PATTERN.matcher(content);
        while (matcher.find()) {
            variables.add(matcher.group(1).trim());
        }
        return variables;
    }

    private void validateTenant(UUID organizationId) {
        UUID currentTenant = TenantContext.getCurrentTenant();
        if (currentTenant != null && !currentTenant.equals(organizationId)) {
            throw new SecurityException("Tenant isolation boundary violation: operation denied.");
        }
    }
}
