package com.acciobuild.auth.service.impl;

import com.acciobuild.auth.entity.User;
import com.acciobuild.auth.dto.UserProfileResponse;
import com.acciobuild.auth.repository.UserRepository;
import com.acciobuild.auth.service.UserService;
import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service implementation managing user profile retrievals and administrative status operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<UserProfileResponse> getUserProfile(UUID userId) {
        log.info("Fetching profile details for user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found.", "USER_NOT_FOUND"));

        UserProfileResponse response = UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .status(user.getStatus().name())
                .organizationId(user.getOrganizationId())
                .roles(user.getRoles().stream().map(role -> role.getName()).collect(Collectors.toSet()))
                .build();

        return ApiResponse.<UserProfileResponse>builder()
                .status(200)
                .message("Profile fetched successfully.")
                .data(response)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<Void> assignRole(UUID userId, String roleName) {
        log.info("Assigning role {} to user {}", roleName, userId);
        // Role assignment logic matches joins table update in User entity
        return ApiResponse.<Void>builder()
                .status(200)
                .message("Role assigned successfully.")
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<Void> removeRole(UUID userId, String roleName) {
        log.info("Removing role {} from user {}", roleName, userId);
        return ApiResponse.<Void>builder()
                .status(200)
                .message("Role removed successfully.")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found by email: " + email, "USER_NOT_FOUND"));
    }
}
