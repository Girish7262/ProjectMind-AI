package com.acciobuild.ai.domain.specification;

import com.acciobuild.ai.domain.model.AiContext;
import org.springframework.data.jpa.domain.Specification;
import java.util.UUID;

/**
 * Mappings for dynamic filter specifications on context models.
 */
public final class AiContextSpecification {

    private AiContextSpecification() {}

    public static Specification<AiContext> hasConversationId(UUID conversationId) {
        return (root, query, cb) -> conversationId == null ? null :
                cb.equal(root.get("conversationId"), conversationId);
    }
}
