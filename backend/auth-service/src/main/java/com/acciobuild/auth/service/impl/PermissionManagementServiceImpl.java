package com.acciobuild.auth.service.impl;

import com.acciobuild.auth.dto.PermissionResponse;
import com.acciobuild.auth.entity.Permission;
import com.acciobuild.auth.repository.PermissionRepository;
import com.acciobuild.auth.service.PermissionManagementService;
import com.acciobuild.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation managing Permission entity queries.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionManagementServiceImpl implements PermissionManagementService {

    private final PermissionRepository permissionRepository;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<PermissionResponse>> getPermissions() {
        log.info("Fetching all system permissions");
        List<Permission> permissions = permissionRepository.findAll();
        
        List<PermissionResponse> responses = permissions.stream()
                .map(this::mapToPermissionResponse)
                .collect(Collectors.toList());

        return ApiResponse.<List<PermissionResponse>>builder()
                .status(200)
                .message("All permissions fetched successfully.")
                .data(responses)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<PermissionResponse>> getPermissionsByModule(String module) {
        log.info("Fetching permissions for module: {}", module);
        List<Permission> permissions = permissionRepository.findByModule(module);
        
        List<PermissionResponse> responses = permissions.stream()
                .map(this::mapToPermissionResponse)
                .collect(Collectors.toList());

        return ApiResponse.<List<PermissionResponse>>builder()
                .status(200)
                .message("Permissions for module " + module + " fetched successfully.")
                .data(responses)
                .build();
    }

    /**
     * Maps Permission entity to PermissionResponse DTO.
     */
    private PermissionResponse mapToPermissionResponse(Permission p) {
        if (p == null) return null;
        return PermissionResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .code(p.getCode())
                .description(p.getDescription())
                .module(p.getModule())
                .build();
    }
}
