package com.acciobuild.auth.service;

import com.acciobuild.auth.dto.UserSession;
import java.util.Optional;
import java.util.UUID;

/**
 * Service interface managing user session records in Redis.
 */
public interface RedisTokenService {

    void saveSession(UserSession session);

    Optional<UserSession> getSession(UUID userId, UUID sessionId);

    void deleteSession(UUID userId, UUID sessionId);

    void deleteAllSessions(UUID userId);

    boolean hasSession(UUID userId, UUID sessionId);
}
