package com.acciobuild.knowledge.domain.repository;

import com.acciobuild.knowledge.domain.model.KnowledgeDocument;
import com.acciobuild.knowledge.domain.model.KnowledgeMetadata;
import com.acciobuild.knowledge.domain.specification.KnowledgeDocumentSpecification;
import com.acciobuild.knowledge.enums.ApprovalStatus;
import com.acciobuild.knowledge.enums.KnowledgeSourceType;
import com.acciobuild.knowledge.enums.KnowledgeStatus;
import com.acciobuild.knowledge.enums.KnowledgeVisibility;
import com.acciobuild.knowledge.enums.ReviewStatus;
import com.acciobuild.knowledge.multitenancy.TenantContext;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * DataJpaTest suite verifying repository query executions, specifications, and multi-tenant context filters.
 */
@DataJpaTest(includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
        com.acciobuild.knowledge.multitenancy.TenantAspect.class
}))
@org.springframework.context.annotation.Import(org.springframework.boot.autoconfigure.aop.AopAutoConfiguration.class)
public class KnowledgeDocumentRepositoryTest {

    @Autowired
    private KnowledgeDocumentRepository documentRepository;

    @Autowired
    private EntityManager entityManager;

    private UUID orgA;
    private UUID orgB;
    private UUID projectId;

    @BeforeEach
    void setUp() {
        orgA = UUID.randomUUID();
        orgB = UUID.randomUUID();
        projectId = UUID.randomUUID();
        TenantContext.clear();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void testTenantIsolation_Success() {
        // Document inside Org A context
        KnowledgeDocument docA = new KnowledgeDocument();
        docA.setId(UUID.randomUUID());
        docA.setProjectId(projectId);
        docA.setOrganizationId(orgA);
        docA.setTitle("Org A Knowledge doc");
        docA.setSlug("org-a-slug");
        docA.setContentType("text/markdown");
        docA.setStatus(KnowledgeStatus.DRAFT);
        docA.setVisibility(KnowledgeVisibility.INTERNAL);
        docA.setSourceType(KnowledgeSourceType.MANUAL);
        docA.setCreatedBy(UUID.randomUUID());
        docA.setUpdatedBy(UUID.randomUUID());

        KnowledgeMetadata metaA = new KnowledgeMetadata();
        metaA.setDocument(docA);
        metaA.setLanguage("en");
        metaA.setReviewStatus(ReviewStatus.PENDING);
        metaA.setApprovalStatus(ApprovalStatus.PENDING);
        docA.setMetadata(metaA);

        // Document inside Org B context
        KnowledgeDocument docB = new KnowledgeDocument();
        docB.setId(UUID.randomUUID());
        docB.setProjectId(projectId);
        docB.setOrganizationId(orgB);
        docB.setTitle("Org B Knowledge doc");
        docB.setSlug("org-b-slug");
        docB.setContentType("text/markdown");
        docB.setStatus(KnowledgeStatus.DRAFT);
        docB.setVisibility(KnowledgeVisibility.INTERNAL);
        docB.setSourceType(KnowledgeSourceType.MANUAL);
        docB.setCreatedBy(UUID.randomUUID());
        docB.setUpdatedBy(UUID.randomUUID());

        KnowledgeMetadata metaB = new KnowledgeMetadata();
        metaB.setDocument(docB);
        metaB.setLanguage("en");
        metaB.setReviewStatus(ReviewStatus.PENDING);
        metaB.setApprovalStatus(ApprovalStatus.PENDING);
        docB.setMetadata(metaB);

        documentRepository.save(docA);
        documentRepository.save(docB);
        entityManager.flush();
        entityManager.clear();

        // 1. Verify Org A isolated view
        TenantContext.setCurrentTenant(orgA);
        List<KnowledgeDocument> listA = documentRepository.findAll();
        assertEquals(1, listA.size());
        assertEquals("org-a-slug", listA.get(0).getSlug());

        // 2. Verify Org B isolated view
        TenantContext.setCurrentTenant(orgB);
        List<KnowledgeDocument> listB = documentRepository.findAll();
        assertEquals(1, listB.size());
        assertEquals("org-b-slug", listB.get(0).getSlug());
    }

    @Test
    void testSpecifications_KeywordSearch_Success() {
        TenantContext.setCurrentTenant(orgA);

        KnowledgeDocument doc = new KnowledgeDocument();
        doc.setId(UUID.randomUUID());
        doc.setProjectId(projectId);
        doc.setOrganizationId(orgA);
        doc.setTitle("AccioBuild Vector Databases deployment guide");
        doc.setSlug("vector-db-guide");
        doc.setContentType("text/markdown");
        doc.setStatus(KnowledgeStatus.PUBLISHED);
        doc.setVisibility(KnowledgeVisibility.INTERNAL);
        doc.setSourceType(KnowledgeSourceType.MANUAL);
        doc.setCreatedBy(UUID.randomUUID());
        doc.setUpdatedBy(UUID.randomUUID());

        KnowledgeMetadata meta = new KnowledgeMetadata();
        meta.setDocument(doc);
        meta.setLanguage("en");
        meta.setReviewStatus(ReviewStatus.APPROVED);
        meta.setApprovalStatus(ApprovalStatus.APPROVED);
        doc.setMetadata(meta);

        documentRepository.save(doc);
        entityManager.flush();
        entityManager.clear();

        Specification<KnowledgeDocument> spec = KnowledgeDocumentSpecification.hasKeyword("databases");
        Page<KnowledgeDocument> page = documentRepository.findAll(spec, PageRequest.of(0, 10));

        assertEquals(1, page.getTotalElements());
        assertEquals("vector-db-guide", page.getContent().get(0).getSlug());
    }
}
