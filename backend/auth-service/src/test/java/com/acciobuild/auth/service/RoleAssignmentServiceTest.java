package com.acciobuild.auth.service;

import com.acciobuild.auth.dto.UserRolesResponse;
import com.acciobuild.auth.entity.Role;
import com.acciobuild.auth.entity.User;
import com.acciobuild.auth.repository.RoleRepository;
import com.acciobuild.auth.repository.UserRepository;
import com.acciobuild.auth.service.impl.RoleAssignmentServiceImpl;
import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests validating user roles assignment flows, hierarchy constraints, and privilege escalations.
 */
@ExtendWith(MockitoExtension.class)
public class RoleAssignmentServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private AuditService auditService;
    @Mock private RedisTokenService redisTokenService;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private RoleHierarchy roleHierarchy;
    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

    @InjectMocks
    private RoleAssignmentServiceImpl roleAssignmentService;

    private User targetUser;
    private Role targetRole;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
        
        targetUser = new User();
        targetUser.setId(UUID.randomUUID());
        targetUser.setEmail("developer@acciobuild.com");
        targetUser.setRoles(new HashSet<>());

        targetRole = new Role();
        targetRole.setId(UUID.randomUUID());
        targetRole.setName("ORG_ADMIN");
    }

    @Test
    void testAssignRole_PrivilegeEscalation_Failure() {
        // Mock Actor authentication - actor is only a DEVELOPER
        when(securityContext.getAuthentication()).thenReturn(authentication);
        Collection<GrantedAuthority> actorAuthorities = List.of(new SimpleGrantedAuthority("ROLE_DEVELOPER"));
        doReturn(actorAuthorities).when(authentication).getAuthorities();

        // Target role requires ROLE_ORG_ADMIN, but ORG_ADMIN is not reachable from DEVELOPER
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_DEVELOPER"), new SimpleGrantedAuthority("ROLE_VIEWER")))
                .when(roleHierarchy).getReachableGrantedAuthorities(actorAuthorities);

        // Attempting to assign ORG_ADMIN should trigger a privilege escalation error
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            roleAssignmentService.assignRoleToUser(targetUser.getId(), "ORG_ADMIN");
        });

        assertEquals("PRIVILEGE_ESCALATION", exception.getErrorCode());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testAssignRole_Success() {
        // Mock Actor authentication - actor is SUPER_ADMIN
        when(securityContext.getAuthentication()).thenReturn(authentication);
        Collection<GrantedAuthority> actorAuthorities = List.of(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"));
        doReturn(actorAuthorities).when(authentication).getAuthorities();

        when(userRepository.findById(targetUser.getId())).thenReturn(Optional.of(targetUser));
        when(roleRepository.findByName("ORG_ADMIN")).thenReturn(Optional.of(targetRole));
        when(userRepository.save(any(User.class))).thenReturn(targetUser);

        ApiResponse<UserRolesResponse> response = roleAssignmentService.assignRoleToUser(targetUser.getId(), "ORG_ADMIN");

        assertNotNull(response);
        assertEquals(200, response.getStatus());
        verify(redisTokenService).deleteAllSessions(targetUser.getId());
        verify(refreshTokenService).revokeAllTokens(targetUser.getId());
    }
}
