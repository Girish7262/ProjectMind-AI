package com.acciobuild.organization.service;

import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.common.exception.BusinessException;
import com.acciobuild.organization.client.AuthServiceClient;
import com.acciobuild.organization.domain.model.Organization;
import com.acciobuild.organization.domain.model.OrganizationMember;
import com.acciobuild.organization.domain.model.OrganizationSettings;
import com.acciobuild.organization.domain.repository.OrganizationMemberRepository;
import com.acciobuild.organization.domain.repository.OrganizationRepository;
import com.acciobuild.organization.domain.repository.OrganizationSettingsRepository;
import com.acciobuild.organization.dto.OrganizationDto;
import com.acciobuild.organization.dto.OrganizationRequest;
import com.acciobuild.organization.dto.UserResponse;
import com.acciobuild.organization.enums.MemberRole;
import com.acciobuild.organization.enums.MemberStatus;
import com.acciobuild.organization.enums.OrganizationStatus;
import com.acciobuild.organization.exception.DuplicateOrganizationException;
import com.acciobuild.organization.exception.InvalidMembershipOperationException;
import com.acciobuild.organization.service.impl.MembershipServiceImpl;
import com.acciobuild.organization.service.impl.OrganizationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests validating Organization aggregate rules, memberships, and sole owner protections.
 */
@ExtendWith(MockitoExtension.class)
public class OrganizationServiceTest {

    @Mock private OrganizationRepository organizationRepository;
    @Mock private OrganizationSettingsRepository settingsRepository;
    @Mock private OrganizationMemberRepository memberRepository;
    @Mock private AuthServiceClient authServiceClient;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks private OrganizationServiceImpl organizationService;
    @InjectMocks private MembershipServiceImpl membershipService;

    private OrganizationRequest sampleRequest;
    private UUID creatorId;
    private UUID orgId;

    @BeforeEach
    void setUp() {
        creatorId = UUID.randomUUID();
        orgId = UUID.randomUUID();

        sampleRequest = OrganizationRequest.builder()
                .organizationCode("accio-corp")
                .organizationName("Accio Corporation")
                .displayName("Accio Corp")
                .country("United States")
                .timezone("America/New_York")
                .build();
    }

    @Test
    void testCreateOrganization_CodeCollision_Failure() {
        when(organizationRepository.existsByOrganizationCode("accio-corp")).thenReturn(true);

        assertThrows(DuplicateOrganizationException.class, () -> {
            organizationService.createOrganization(sampleRequest, creatorId);
        });

        verify(organizationRepository, never()).save(any(Organization.class));
    }

    @Test
    void testCreateOrganization_Success() {
        when(organizationRepository.existsByOrganizationCode("accio-corp")).thenReturn(false);
        when(organizationRepository.existsByOrganizationName("Accio Corporation")).thenReturn(false);

        Organization sampleOrg = new Organization();
        sampleOrg.setId(orgId);
        sampleOrg.setOrganizationCode("accio-corp");
        sampleOrg.setOrganizationName("Accio Corporation");
        sampleOrg.setDisplayName("Accio Corp");
        sampleOrg.setStatus(OrganizationStatus.ACTIVE);

        when(organizationRepository.save(any(Organization.class))).thenReturn(sampleOrg);

        ApiResponse<OrganizationDto> response = organizationService.createOrganization(sampleRequest, creatorId);

        assertNotNull(response);
        assertEquals(201, response.getStatus()); // 201 Created
        verify(memberRepository).save(any(OrganizationMember.class));
        verify(eventPublisher).publishEvent(any(Object.class));
    }

    @Test
    void testSoleOwnerDemotionBlock_Failure() {
        OrganizationMember soleOwner = new OrganizationMember();
        soleOwner.setUserId(creatorId);
        soleOwner.setRole(MemberRole.OWNER);

        when(memberRepository.findByOrganizationIdAndUserId(orgId, creatorId)).thenReturn(Optional.of(soleOwner));
        when(memberRepository.findByOrganizationId(orgId)).thenReturn(Collections.singletonList(soleOwner));

        assertThrows(InvalidMembershipOperationException.class, () -> {
            membershipService.updateMemberRole(orgId, creatorId, MemberRole.ADMIN);
        });
    }

    @Test
    void testOwnershipTransfer_Success() {
        Organization org = new Organization();
        org.setId(orgId);

        OrganizationMember oldOwner = new OrganizationMember();
        oldOwner.setUserId(creatorId);
        oldOwner.setRole(MemberRole.OWNER);
        oldOwner.setOrganization(org);

        UUID newOwnerId = UUID.randomUUID();
        OrganizationMember candidate = new OrganizationMember();
        candidate.setUserId(newOwnerId);
        candidate.setRole(MemberRole.ADMIN);
        candidate.setOrganization(org);

        when(memberRepository.findByOrganizationIdAndUserId(orgId, newOwnerId)).thenReturn(Optional.of(candidate));
        when(memberRepository.findByOrganizationId(orgId)).thenReturn(List.of(oldOwner, candidate));
        when(memberRepository.save(any(OrganizationMember.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When promoting candidate to OWNER, it demotes creatorId to ADMIN
        ApiResponse<com.acciobuild.organization.dto.MemberDto> response = 
                membershipService.updateMemberRole(orgId, newOwnerId, MemberRole.OWNER);

        assertNotNull(response);
        assertEquals(MemberRole.OWNER.name(), response.getData().getRole());
        assertEquals(MemberRole.ADMIN, oldOwner.getRole());
        verify(memberRepository).save(oldOwner);
    }
}
