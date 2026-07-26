package com.acciobuild.knowledge.dto;

import com.acciobuild.knowledge.enums.EmbeddingModel;
import com.acciobuild.knowledge.enums.EmbeddingProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

/**
 * Data Transfer Object representing the configuration profile of a vector/embedding provider.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmbeddingConfiguration implements Serializable {

    private static final long serialVersionUID = 1L;

    private EmbeddingProvider provider;
    private EmbeddingModel defaultModel;
    private String endpointUrl;
    private String apiKeyVaultRef;
    private int defaultDimension;
    private boolean active;
}
