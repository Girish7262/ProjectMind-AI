package com.acciobuild.ai.domain.specification;

import com.acciobuild.ai.domain.model.AiConversationMemory;
import com.acciobuild.ai.enums.MemoryScope;
import org.springframework.data.jpa.domain.Specification;
import java.util.UUID;

/**
 * Mappings for dynamic filter specifications on memory scope.
 */
public final class MemorySpecification {

    private MemorySpecification() {}

    public static Specification<AiConversationMemory> hasConversationId(UUID conversationId) {
        return (root, query, cb) -> conversationId == null ? null :
                cb.equal(root.get("conversationId"), conversationId);
    }

    public static Specification<AiConversationMemory> hasScope(MemoryScope scope) {
        return (root, query, cb) -> scope == null ? null : cb.equal(root.get("memoryScope"), scope);
    }
}
