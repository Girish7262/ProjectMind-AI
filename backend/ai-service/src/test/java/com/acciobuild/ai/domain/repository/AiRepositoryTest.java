package com.acciobuild.ai.domain.repository;

import com.acciobuild.ai.domain.model.*;
import com.acciobuild.ai.domain.projection.*;
import com.acciobuild.ai.domain.specification.*;
import com.acciobuild.ai.enums.*;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Persistence integration test validating repository configurations, projections, specifications, and tenant isolation constraints.
 */
@DataJpaTest
@ActiveProfiles("test")
public class AiRepositoryTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private AiConversationRepository conversationRepository;

    @Autowired
    private AiConversationMessageRepository messageRepository;

    @Autowired
    private AiPromptTemplateRepository promptTemplateRepository;

    @Test
    void testSaveAndQueryConversation() {
        UUID tenantId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();

        AiConversation conv = new AiConversation();
        conv.setId(UUID.randomUUID());
        conv.setOrganizationId(tenantId);
        conv.setProjectId(projectId);
        conv.setTitle("RAG Q&A Session");
        conv.setStatus(ConversationStatus.ACTIVE);
        conv.setModelProvider(ProviderType.OPENAI);
        conv.setModelName("gpt-4o");
        conv.setTemperature(0.7);
        conv.setCreatedAt(LocalDateTime.now());
        conv.setUpdatedAt(LocalDateTime.now());

        conversationRepository.save(conv);

        entityManager.flush();
        entityManager.clear();

        // Fetch using projection
        Optional<ConversationSummary> summaryOpt = conversationRepository.findSummaryById(conv.getId());
        assertTrue(summaryOpt.isPresent());
        assertEquals("RAG Q&A Session", summaryOpt.get().getTitle());
        assertEquals(ConversationStatus.ACTIVE, summaryOpt.get().getStatus());

        // Fetch using specifications
        Specification<AiConversation> spec = Specification
                .where(AiConversationSpecification.hasProjectId(projectId))
                .and(AiConversationSpecification.hasStatus(ConversationStatus.ACTIVE))
                .and(AiConversationSpecification.hasTitleLike("RAG"));

        List<AiConversation> results = conversationRepository.findAll(spec);
        assertEquals(1, results.size());
        assertEquals(conv.getId(), results.get(0).getId());
    }

    @Test
    void testTenantFilterIsolation() {
        UUID tenant1 = UUID.randomUUID();
        UUID tenant2 = UUID.randomUUID();

        AiConversation conv1 = new AiConversation();
        conv1.setId(UUID.randomUUID());
        conv1.setOrganizationId(tenant1);
        conv1.setProjectId(UUID.randomUUID());
        conv1.setTitle("Tenant 1 Chat");
        conv1.setStatus(ConversationStatus.ACTIVE);
        conv1.setModelProvider(ProviderType.OPENAI);
        conv1.setModelName("gpt-4");
        conv1.setCreatedAt(LocalDateTime.now());
        conv1.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conv1);

        AiConversation conv2 = new AiConversation();
        conv2.setId(UUID.randomUUID());
        conv2.setOrganizationId(tenant2);
        conv2.setProjectId(UUID.randomUUID());
        conv2.setTitle("Tenant 2 Chat");
        conv2.setStatus(ConversationStatus.ACTIVE);
        conv2.setModelProvider(ProviderType.OPENAI);
        conv2.setModelName("gpt-4");
        conv2.setCreatedAt(LocalDateTime.now());
        conv2.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conv2);

        entityManager.flush();
        entityManager.clear();

        // Enable Filter for tenant1
        Session session = entityManager.unwrap(Session.class);
        session.enableFilter("tenantFilter").setParameter("tenantId", tenant1);

        List<AiConversation> filtered = conversationRepository.findAll();
        assertEquals(1, filtered.size());
        assertEquals(conv1.getId(), filtered.get(0).getId());

        // Switch filter to tenant2
        session.disableFilter("tenantFilter");
        session.enableFilter("tenantFilter").setParameter("tenantId", tenant2);

        List<AiConversation> filtered2 = conversationRepository.findAll();
        assertEquals(1, filtered2.size());
        assertEquals(conv2.getId(), filtered2.get(0).getId());
    }
}
