package com.acciobuild.project.service;

import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.project.dto.ProjectMemberDto;
import java.util.List;
import java.util.UUID;

/**
 * Service interface managing project collaborator assignments and security roles.
 */
public interface ProjectMemberService {

    /**
     * Enrolls a user member as collaborator into a project.
     */
    ApiResponse<ProjectMemberDto> addMember(UUID projectId, UUID userId, String role);

    /**
     * Modifies collaborator access role within a project.
     */
    ApiResponse<ProjectMemberDto> updateMemberRole(UUID projectId, UUID userId, String role);

    /**
     * Removes collaborator association from a project.
     */
    ApiResponse<Void> removeMember(UUID projectId, UUID userId);

    /**
     * Retrieves all collaborators enrolled in a project.
     */
    ApiResponse<List<ProjectMemberDto>> getMembers(UUID projectId);
}
