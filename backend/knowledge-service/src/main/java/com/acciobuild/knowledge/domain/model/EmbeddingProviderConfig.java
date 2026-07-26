package com.acciobuild.knowledge.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import java.util.UUID;

/**
 * JPA Entity representing the configuration parameters for an embedding API provider.
 */
@Entity
@Table(name = "embedding_provider_config")
@Getter
@Setter
public class EmbeddingProviderConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "provider_name", nullable = false, unique = true, length = 50)
    private String providerName;

    @Column(name = "api_key_vault_ref", nullable = false, length = 100)
    private String apiKeyVaultRef;

    @Column(name = "endpoint_url", nullable = false, length = 250)
    private String endpointUrl;

    @Column(name = "default_model", nullable = false, length = 50)
    private String defaultModel;

    @Column(name = "default_dimension", nullable = false)
    private int defaultDimension;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
}
