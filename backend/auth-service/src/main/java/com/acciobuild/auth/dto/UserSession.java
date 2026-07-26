package com.acciobuild.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Model class representing user session details stored in Redis.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSession implements Serializable {
    private static final long serialVersionUID = 1L;

    private UUID userId;
    private UUID sessionId;
    private String refreshToken;
    private String ipAddress;
    private String device;
    private String browser;
    private LocalDateTime loginTime;
    private LocalDateTime expiresAt;
}
