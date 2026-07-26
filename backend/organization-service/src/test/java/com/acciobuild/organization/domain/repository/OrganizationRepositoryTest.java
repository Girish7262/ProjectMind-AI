package com.acciobuild.organization.domain.repository;

import com.acciobuild.organization.domain.model.Organization;
import com.acciobuild.organization.domain.model.OrganizationMember;
import com.acciobuild.organization.domain.model.OrganizationSettings;
import com.acciobuild.organization.domain.specification.OrganizationSpecification;
import com.acciobuild.organization.enums.MemberRole;
import com.acciobuild.organization.enums.MemberStatus;
import com.acciobuild.organization.enums.OrganizationStatus;
import com.acciobuild.organization.multitenancy.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests validating Repository CRUD actions, JPA specifications, and Multi-Tenant filters isolation.
 */
@DataJpaTest
public class OrganizationRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private OrganizationMemberRepository memberRepository;

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void testCreateAndRetrieveOrganization() {
        Organization org = new Organization();
        org.setOrganizationCode("accio-corp");
        org.setOrganizationName("Accio Corporation");
        org.setDisplayName("Accio Corp");
        org.setCountry("United States");
        org.setTimezone("America/New_York");
        org.setStatus(OrganizationStatus.ACTIVE);

        OrganizationSettings settings = new OrganizationSettings();
        settings.setOrganization(org);
        settings.setAiEnabled(true);
        settings.setKnowledgeSharingEnabled(true);
        settings.setDefaultVisibility("PRIVATE");
        org.setSettings(settings);

        Organization saved = organizationRepository.save(org);
        assertNotNull(saved.getId());

        entityManager.flush();
        entityManager.clear();

        Optional<Organization> retrieved = organizationRepository.findWithSettingsById(saved.getId());
        assertTrue(retrieved.isPresent());
        assertEquals("accio-corp", retrieved.get().getOrganizationCode());
        assertNotNull(retrieved.get().getSettings());
        assertTrue(retrieved.get().getSettings().isAiEnabled());
    }

    @Test
    void testDynamicSpecificationsSearch() {
        Organization org1 = createSampleOrg("google", "Google LLC", "USA", "IT");
        Organization org2 = createSampleOrg("accio-corp", "Accio Corporation", "USA", "AI");
        organizationRepository.save(org1);
        organizationRepository.save(org2);

        entityManager.flush();
        entityManager.clear();

        Specification<Organization> spec = Specification
                .where(OrganizationSpecification.hasKeyword("Accio"))
                .and(OrganizationSpecification.hasCountry("USA"))
                .and(OrganizationSpecification.hasIndustry("AI"));

        Page<Organization> results = organizationRepository.findAll(spec, PageRequest.of(0, 10));
        assertEquals(1, results.getTotalElements());
        assertEquals("accio-corp", results.getContent().get(0).getOrganizationCode());
    }

    @Test
    void testMultiTenantIsolationFilter() {
        // Create Tenant A Org & Member
        Organization orgA = createSampleOrg("tenant-a", "Tenant A Corp", "USA", "Finance");
        orgA = organizationRepository.save(orgA);

        OrganizationMember memberA = new OrganizationMember();
        memberA.setOrganization(orgA);
        memberA.setUserId(UUID.randomUUID());
        memberA.setRole(MemberRole.OWNER);
        memberA.setStatus(MemberStatus.ACTIVE);
        memberRepository.save(memberA);

        // Create Tenant B Org & Member
        Organization orgB = createSampleOrg("tenant-b", "Tenant B Corp", "UK", "Marketing");
        orgB = organizationRepository.save(orgB);

        OrganizationMember memberB = new OrganizationMember();
        memberB.setOrganization(orgB);
        memberB.setUserId(UUID.randomUUID());
        memberB.setRole(MemberRole.OWNER);
        memberB.setStatus(MemberStatus.ACTIVE);
        memberRepository.save(memberB);

        entityManager.flush();
        entityManager.clear();

        // 1. Set context to Tenant A and verify query is restricted to Tenant A member
        TenantContext.setCurrentTenant(orgA.getId());
        // Manually enable filter as AOP aspect handles repo beans, but here we query directly
        entityManager.getEntityManager().unwrap(org.hibernate.Session.class)
                .enableFilter("tenantFilter")
                .setParameter("tenantId", orgA.getId());

        var membersTenantA = memberRepository.findAll();
        assertEquals(1, membersTenantA.size());
        assertEquals(memberA.getUserId(), membersTenantA.get(0).getUserId());

        // 2. Set context to Tenant B and verify query is restricted to Tenant B member
        TenantContext.setCurrentTenant(orgB.getId());
        entityManager.getEntityManager().unwrap(org.hibernate.Session.class)
                .enableFilter("tenantFilter")
                .setParameter("tenantId", orgB.getId());

        var membersTenantB = memberRepository.findAll();
        assertEquals(1, membersTenantB.size());
        assertEquals(memberB.getUserId(), membersTenantB.get(0).getUserId());
    }

    private Organization createSampleOrg(String code, String name, String country, String industry) {
        Organization org = new Organization();
        org.setOrganizationCode(code);
        org.setOrganizationName(name);
        org.setDisplayName(name);
        org.setCountry(country);
        org.setTimezone("GMT");
        org.setStatus(OrganizationStatus.ACTIVE);
        org.setIndustry(industry);
        return org;
    }
}
