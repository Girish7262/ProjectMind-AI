package com.acciobuild.knowledge.service.impl;

import com.acciobuild.common.dto.ApiResponse;
import com.acciobuild.knowledge.domain.model.KnowledgeCategory;
import com.acciobuild.knowledge.domain.repository.KnowledgeCategoryRepository;
import com.acciobuild.knowledge.dto.KnowledgeCategoryDto;
import com.acciobuild.knowledge.service.KnowledgeCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service implementation for Knowledge Categories management.
 */
@Service
@RequiredArgsConstructor
public class KnowledgeCategoryServiceImpl implements KnowledgeCategoryService {

    private final KnowledgeCategoryRepository categoryRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<KnowledgeCategoryDto> createCategory(KnowledgeCategoryDto dto) {
        KnowledgeCategory cat = new KnowledgeCategory();
        cat.setId(UUID.randomUUID());
        cat.setProjectId(dto.getProjectId());
        cat.setOrganizationId(dto.getOrganizationId() != null ? dto.getOrganizationId() : UUID.randomUUID());
        cat.setName(dto.getName());
        cat.setDescription(dto.getDescription());
        cat.setColor(dto.getColor() != null ? dto.getColor() : "#6366f1");
        cat.setIcon(dto.getIcon() != null ? dto.getIcon() : "folder");

        KnowledgeCategory saved = categoryRepository.save(cat);
        return ApiResponse.<KnowledgeCategoryDto>builder()
                .status(201)
                .message("Category created successfully.")
                .data(mapToDto(saved))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<KnowledgeCategoryDto>> getCategories(UUID projectId) {
        List<KnowledgeCategoryDto> list = categoryRepository.findByProjectId(projectId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        return ApiResponse.<List<KnowledgeCategoryDto>>builder()
                .status(200)
                .message("Categories fetched.")
                .data(list)
                .build();
    }

    private KnowledgeCategoryDto mapToDto(KnowledgeCategory cat) {
        return KnowledgeCategoryDto.builder()
                .id(cat.getId())
                .projectId(cat.getProjectId())
                .organizationId(cat.getOrganizationId())
                .name(cat.getName())
                .description(cat.getDescription())
                .color(cat.getColor())
                .icon(cat.getIcon())
                .build();
    }
}
