package com.acciobuild.ai.domain.specification;

import com.acciobuild.ai.domain.model.AiConversationMessage;
import org.springframework.data.jpa.domain.Specification;
import java.util.UUID;

/**
 * Mappings for dynamic filter specifications on messages.
 */
public final class ConversationMessageSpecification {

    private ConversationMessageSpecification() {}

    public static Specification<AiConversationMessage> hasConversationId(UUID conversationId) {
        return (root, query, cb) -> conversationId == null ? null :
                cb.equal(root.get("conversation").get("id"), conversationId);
    }

    public static Specification<AiConversationMessage> hasContentLike(String keyword) {
        return (root, query, cb) -> (keyword == null || keyword.isEmpty()) ? null :
                cb.like(cb.lower(root.get("content")), "%" + keyword.toLowerCase() + "%");
    }
}
