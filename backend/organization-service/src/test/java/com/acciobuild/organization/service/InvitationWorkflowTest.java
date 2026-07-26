package com.acciobuild.organization.service;

import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.organization.client.AuthServiceClient;
import com.acciobuild.organization.domain.event.InvitationAcceptedEvent;
import com.acciobuild.organization.domain.model.Organization;
import com.acciobuild.organization.domain.model.OrganizationInvitation;
import com.acciobuild.organization.domain.repository.OrganizationInvitationRepository;
import com.acciobuild.organization.domain.repository.OrganizationMemberRepository;
import com.acciobuild.organization.domain.repository.OrganizationRepository;
import com.acciobuild.organization.dto.UserResponse;
import com.acciobuild.organization.exception.InvitationExpiredException;
import com.acciobuild.organization.scheduler.CleanupScheduler;
import com.acciobuild.organization.service.impl.InvitationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests validating invitation acceptance, token expiration, and database cleanups.
 */
@ExtendWith(MockitoExtension.class)
public class InvitationWorkflowTest {

    @Mock private OrganizationInvitationRepository invitationRepository;
    @Mock private OrganizationRepository organizationRepository;
    @Mock private OrganizationMemberRepository memberRepository;
    @Mock private AuthServiceClient authServiceClient;
    @Mock private MembershipService membershipService;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks private InvitationServiceImpl invitationService;
    @InjectMocks private CleanupScheduler cleanupScheduler;

    private OrganizationInvitation invitation;
    private UUID userId;
    private String token;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        token = UUID.randomUUID().toString();

        Organization org = new Organization();
        org.setId(UUID.randomUUID());

        invitation = new OrganizationInvitation();
        invitation.setId(UUID.randomUUID());
        invitation.setOrganization(org);
        invitation.setEmail("newuser@company.com");
        invitation.setInviteToken(token);
        invitation.setAccepted(false);
        invitation.setExpiresAt(LocalDateTime.now().plusDays(1));
    }

    @Test
    void testAcceptInvitation_Success() {
        when(invitationRepository.findByInviteToken(token)).thenReturn(Optional.of(invitation));
        when(invitationRepository.save(any(OrganizationInvitation.class))).thenReturn(invitation);

        ApiResponse<Void> response = invitationService.acceptInvitation(token, userId);

        assertNotNull(response);
        assertEquals(200, response.getStatus());
        assertTrue(invitation.isAccepted());
        verify(membershipService).addMember(any(), eq(userId), any());
        verify(eventPublisher).publishEvent(any(InvitationAcceptedEvent.class));
    }

    @Test
    void testAcceptInvitation_Expired_Failure() {
        invitation.setExpiresAt(LocalDateTime.now().minusDays(1));
        when(invitationRepository.findByInviteToken(token)).thenReturn(Optional.of(invitation));

        assertThrows(InvitationExpiredException.class, () -> {
            invitationService.acceptInvitation(token, userId);
        });

        verify(membershipService, never()).addMember(any(), any(), any());
    }

    @Test
    void testSchedulerDailyCleanup_Success() {
        when(invitationRepository.deleteExpiredInvitations(any(LocalDateTime.class))).thenReturn(5);

        cleanupScheduler.executeDailyCleanup();

        verify(invitationRepository).deleteExpiredInvitations(any(LocalDateTime.class));
    }
}
