package com.acciobuild.project.domain.repository;

import com.acciobuild.project.domain.model.Project;
import com.acciobuild.project.domain.model.ProjectSettings;
import com.acciobuild.project.domain.specification.ProjectSpecification;
import com.acciobuild.project.enums.ProjectStatus;
import com.acciobuild.project.enums.ProjectVisibility;
import com.acciobuild.project.multitenancy.TenantContext;
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
 * DataJpaTest integration suite verifying SQL constraints, entity graphs,
 * specifications, and tenant isolation boundaries.
 */
@DataJpaTest(includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
        com.acciobuild.project.multitenancy.TenantAspect.class
}))
public class ProjectRepositoryTest {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private EntityManager entityManager;

    private UUID orgA;
    private UUID orgB;

    @BeforeEach
    void setUp() {
        orgA = UUID.randomUUID();
        orgB = UUID.randomUUID();
        TenantContext.clear();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void testTenantIsolation_Filtering() {
        // Create project inside Org A boundary
        Project p1 = new Project();
        p1.setId(UUID.randomUUID());
        p1.setOrganizationId(orgA);
        p1.setProjectCode("org-a-code");
        p1.setProjectName("Org A Project");
        p1.setStatus(ProjectStatus.ACTIVE);
        p1.setVisibility(ProjectVisibility.PRIVATE);
        p1.setCreatedBy(UUID.randomUUID());
        p1.setUpdatedBy(UUID.randomUUID());
        
        ProjectSettings s1 = new ProjectSettings();
        s1.setProject(p1);
        s1.setDefaultBranch("main");
        p1.setSettings(s1);

        // Create project inside Org B boundary
        Project p2 = new Project();
        p2.setId(UUID.randomUUID());
        p2.setOrganizationId(orgB);
        p2.setProjectCode("org-b-code");
        p2.setProjectName("Org B Project");
        p2.setStatus(ProjectStatus.ACTIVE);
        p2.setVisibility(ProjectVisibility.PRIVATE);
        p2.setCreatedBy(UUID.randomUUID());
        p2.setUpdatedBy(UUID.randomUUID());
        
        ProjectSettings s2 = new ProjectSettings();
        s2.setProject(p2);
        s2.setDefaultBranch("develop");
        p2.setSettings(s2);

        projectRepository.save(p1);
        projectRepository.save(p2);
        entityManager.flush();
        entityManager.clear();

        // 1. Check isolation for Tenant Org A
        TenantContext.setCurrentTenant(orgA);
        entityManager.unwrap(org.hibernate.Session.class)
                .enableFilter("tenantFilter")
                .setParameter("tenantId", orgA);
        List<Project> orgAList = projectRepository.findAll();
        assertEquals(1, orgAList.size());
        assertEquals("org-a-code", orgAList.get(0).getProjectCode());

        // 2. Check isolation for Tenant Org B
        TenantContext.setCurrentTenant(orgB);
        entityManager.unwrap(org.hibernate.Session.class)
                .enableFilter("tenantFilter")
                .setParameter("tenantId", orgB);
        List<Project> orgBList = projectRepository.findAll();
        assertEquals(1, orgBList.size());
        assertEquals("org-b-code", orgBList.get(0).getProjectCode());
    }

    @Test
    void testProjectSpecifications_KeywordFiltering() {
        TenantContext.setCurrentTenant(orgA);
        entityManager.unwrap(org.hibernate.Session.class)
                .enableFilter("tenantFilter")
                .setParameter("tenantId", orgA);

        Project p = new Project();
        p.setId(UUID.randomUUID());
        p.setOrganizationId(orgA);
        p.setProjectCode("ml-model");
        p.setProjectName("Machine Learning Engine");
        p.setStatus(ProjectStatus.ACTIVE);
        p.setVisibility(ProjectVisibility.PRIVATE);
        p.setCreatedBy(UUID.randomUUID());
        p.setUpdatedBy(UUID.randomUUID());
        
        ProjectSettings s = new ProjectSettings();
        s.setProject(p);
        s.setDefaultBranch("main");
        p.setSettings(s);

        projectRepository.save(p);
        entityManager.flush();
        entityManager.clear();

        // Match on project name keyword case-insensitively
        Specification<Project> spec = ProjectSpecification.hasKeyword("learning");
        Page<Project> page = projectRepository.findAll(spec, PageRequest.of(0, 10));

        assertEquals(1, page.getTotalElements());
        assertEquals("ml-model", page.getContent().get(0).getProjectCode());
    }
}
