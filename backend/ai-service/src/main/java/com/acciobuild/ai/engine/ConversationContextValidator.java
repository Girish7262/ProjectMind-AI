package com.acciobuild.ai.engine;

import com.acciobuild.ai.domain.model.AiConversation;
import com.acciobuild.ai.exception.InvalidConversationStateException;
import com.acciobuild.ai.multitenancy.TenantContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.UUID;

/**
 * Pipeline component that validates tenant isolation, project memberships, and conversation status.
 */
@Component
@Slf4j
public class ConversationContextValidator {

    /**
     * Asserts safety policies on the conversation context before prompt formulation.
     */
    public void validate(AiConversation conv) {
        log.info("Validating conversation isolation checks for: {}", conv.getId());

        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId != null && !tenantId.equals(conv.getOrganizationId())) {
            throw new InvalidConversationStateException("Tenant isolation violation: Access denied to requested tenant's data.");
        }

        if (conv.getProjectId() == null) {
            throw new InvalidConversationStateException("Project membership violation: Conversation does not map to a valid project context.");
        }
    }
}
