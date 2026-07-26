package com.acciobuild.knowledge.service.impl;

import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.knowledge.domain.model.KnowledgeTag;
import com.acciobuild.knowledge.domain.repository.KnowledgeTagRepository;
import com.acciobuild.knowledge.dto.KnowledgeTagDto;
import com.acciobuild.knowledge.service.KnowledgeTagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service implementation for Knowledge Tags.
 */
@Service
@RequiredArgsConstructor
public class KnowledgeTagServiceImpl implements KnowledgeTagService {

    private final KnowledgeTagRepository tagRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<KnowledgeTagDto> createTag(KnowledgeTagDto dto) {
        KnowledgeTag tag = new KnowledgeTag();
        tag.setId(UUID.randomUUID());
        tag.setProjectId(dto.getProjectId());
        tag.setOrganizationId(dto.getOrganizationId() != null ? dto.getOrganizationId() : UUID.randomUUID());
        tag.setName(dto.getName());
        tag.setColor(dto.getColor() != null ? dto.getColor() : "#6366f1");

        KnowledgeTag saved = tagRepository.save(tag);
        return ApiResponse.<KnowledgeTagDto>builder()
                .status(201)
                .message("Tag created.")
                .data(mapToDto(saved))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<KnowledgeTagDto>> getTags(UUID projectId) {
        List<KnowledgeTagDto> list = tagRepository.findByProjectId(projectId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        return ApiResponse.<List<KnowledgeTagDto>>builder()
                .status(200)
                .message("Tags fetched.")
                .data(list)
                .build();
    }

    private KnowledgeTagDto mapToDto(KnowledgeTag tag) {
        return KnowledgeTagDto.builder()
                .id(tag.getId())
                .projectId(tag.getProjectId())
                .organizationId(tag.getOrganizationId())
                .name(tag.getName())
                .color(tag.getColor())
                .build();
    }
}
