package com.acciobuild.ai.domain.specification;

import com.acciobuild.ai.domain.model.AiConversation;
import com.acciobuild.ai.enums.ConversationStatus;
import com.acciobuild.ai.enums.ProviderType;
import org.springframework.data.jpa.domain.Specification;
import java.util.UUID;

/**
 * Mappings for dynamic filter specifications on AiConversation.
 */
public final class AiConversationSpecification {

    private AiConversationSpecification() {}

    public static Specification<AiConversation> hasProjectId(UUID projectId) {
        return (root, query, cb) -> projectId == null ? null : cb.equal(root.get("projectId"), projectId);
    }

    public static Specification<AiConversation> hasStatus(ConversationStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<AiConversation> hasModelProvider(ProviderType provider) {
        return (root, query, cb) -> provider == null ? null : cb.equal(root.get("modelProvider"), provider);
    }

    public static Specification<AiConversation> hasTitleLike(String keyword) {
        return (root, query, cb) -> (keyword == null || keyword.isEmpty()) ? null :
                cb.like(cb.lower(root.get("title")), "%" + keyword.toLowerCase() + "%");
    }
}
