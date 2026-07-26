package com.acciobuild.auth.service;

import com.acciobuild.auth.dto.ForgotPasswordRequest;
import com.acciobuild.auth.dto.ResetPasswordRequest;
import com.acciobuild.auth.entity.PasswordResetToken;
import com.acciobuild.auth.entity.User;
import com.acciobuild.auth.repository.PasswordResetRepository;
import com.acciobuild.auth.repository.UserRepository;
import com.acciobuild.auth.service.impl.PasswordResetServiceImpl;
import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests validating forgot password recovery request logic and confirmation resets.
 */
@ExtendWith(MockitoExtension.class)
public class PasswordResetServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordResetRepository passwordResetRepository;
    @Mock private PasswordPolicyService passwordPolicyService;
    @Mock private EmailService emailService;
    @Mock private AuditService auditService;
    @Mock private RedisTokenService redisTokenService;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private RedisTemplate<String, Object> redisTemplate;
    @Mock private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private PasswordResetServiceImpl passwordResetService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("user@acciobuild.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setPasswordHash("old_hashed_password");
        user.setOrganizationId(UUID.randomUUID());
        
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void testForgotPassword_Success() {
        ForgotPasswordRequest request = new ForgotPasswordRequest("user@acciobuild.com");
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        
        ApiResponse<Void> response = passwordResetService.forgotPassword(request, "127.0.0.1", "Desktop PC", "Chrome");
        
        assertNotNull(response);
        assertEquals(200, response.getStatus());
        verify(passwordResetRepository).invalidateExistingTokens(user.getId());
        verify(passwordResetRepository).save(any(PasswordResetToken.class));
        verify(emailService).sendResetPasswordEmail(eq(user), anyString());
    }

    @Test
    void testResetPassword_Success() {
        ResetPasswordRequest request = new ResetPasswordRequest("123e4567-e89b-12d3-a456-426614174000", "NewP@ssword123", "NewP@ssword123");
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUser(user);
        resetToken.setToken("123e4567-e89b-12d3-a456-426614174000");
        resetToken.setExpiresAt(LocalDateTime.now().plusHours(1));
        resetToken.setUsed(false);

        when(passwordResetRepository.findByToken(request.getToken())).thenReturn(Optional.of(resetToken));
        when(passwordEncoder.encode(request.getNewPassword())).thenReturn("new_hashed_password");

        ApiResponse<Void> response = passwordResetService.resetPassword(request, "127.0.0.1", "Desktop PC", "Chrome");

        assertNotNull(response);
        assertEquals(200, response.getStatus());
        assertTrue(resetToken.isUsed());
        verify(userRepository).save(user);
        verify(redisTokenService).deleteAllSessions(user.getId());
        verify(refreshTokenService).revokeAllTokens(user.getId());
        verify(emailService).sendPasswordChangedEmail(user);
    }

    @Test
    void testResetPassword_MismatchConfirmation() {
        ResetPasswordRequest request = new ResetPasswordRequest("123e4567-e89b-12d3-a456-426614174000", "NewP@ssword123", "DifferentPassword123");
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUser(user);
        resetToken.setToken("123e4567-e89b-12d3-a456-426614174000");
        resetToken.setExpiresAt(LocalDateTime.now().plusHours(1));
        resetToken.setUsed(false);

        when(passwordResetRepository.findByToken(request.getToken())).thenReturn(Optional.of(resetToken));

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            passwordResetService.resetPassword(request, "127.0.0.1", "Desktop PC", "Chrome");
        });

        assertEquals("PASSWORDS_DO_NOT_MATCH", exception.getErrorCode());
        verify(userRepository, never()).save(any(User.class));
    }
}
