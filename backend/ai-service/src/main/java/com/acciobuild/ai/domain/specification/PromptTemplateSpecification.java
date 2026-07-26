package com.acciobuild.ai.domain.specification;

import com.acciobuild.ai.domain.model.AiPromptTemplate;
import com.acciobuild.ai.enums.PromptStatus;
import org.springframework.data.jpa.domain.Specification;

/**
 * Mappings for dynamic filters on AiPromptTemplate.
 */
public final class PromptTemplateSpecification {

    private PromptTemplateSpecification() {}

    public static Specification<AiPromptTemplate> hasStatus(PromptStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<AiPromptTemplate> hasNameLike(String keyword) {
        return (root, query, cb) -> (keyword == null || keyword.isEmpty()) ? null :
                cb.like(cb.lower(root.get("name")), "%" + keyword.toLowerCase() + "%");
    }
}
