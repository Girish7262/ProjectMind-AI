package com.acciobuild.knowledge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

/**
 * Payload DTO representing a Knowledge Attachment.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeAttachmentDto {
    private UUID id;
    private UUID documentId;
    private String fileName;
    private String contentType;
    private String storagePath;
    private long size;
    private String checksum;
}
