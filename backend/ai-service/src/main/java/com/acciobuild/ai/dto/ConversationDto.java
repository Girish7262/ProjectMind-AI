package com.acciobuild.ai.dto;

import com.acciobuild.ai.enums.ConversationStatus;
import com.acciobuild.ai.enums.ProviderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Data Transfer Object mapping AI conversation session details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDto {
    private UUID id;
    private UUID organizationId;
    private UUID projectId;
    private String title;
    private ConversationStatus status;
    private ProviderType modelProvider;
    private String modelName;
    private Double temperature;
    private String systemInstruction;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<MessageDto> messages;
}
