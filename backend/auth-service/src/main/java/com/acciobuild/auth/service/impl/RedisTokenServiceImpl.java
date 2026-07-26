package com.acciobuild.auth.service.impl;

import com.acciobuild.auth.dto.UserSession;
import com.acciobuild.auth.service.RedisTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Service implementation managing user session records in Redis using key namespace formatting.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RedisTokenServiceImpl implements RedisTokenService {

    private final RedisTemplate<String, Object> redisTemplate;

    private String getSessionKey(UUID userId, UUID sessionId) {
        return String.format("auth:refresh:%s:%s", userId, sessionId);
    }

    private String getUserSessionsPattern(UUID userId) {
        return String.format("auth:refresh:%s:*", userId);
    }

    @Override
    public void saveSession(UserSession session) {
        String key = getSessionKey(session.getUserId(), session.getSessionId());
        log.info("Saving session key: {} in Redis", key);
        
        long durationSeconds = Duration.between(LocalDateTime.now(), session.getExpiresAt()).getSeconds();
        if (durationSeconds > 0) {
            redisTemplate.opsForValue().set(key, session, Duration.ofSeconds(durationSeconds));
        }
    }

    @Override
    public Optional<UserSession> getSession(UUID userId, UUID sessionId) {
        String key = getSessionKey(userId, sessionId);
        log.debug("Querying session key: {} in Redis", key);
        
        Object sessionObj = redisTemplate.opsForValue().get(key);
        if (sessionObj instanceof UserSession) {
            return Optional.of((UserSession) sessionObj);
        }
        return Optional.empty();
    }

    @Override
    public void deleteSession(UUID userId, UUID sessionId) {
        String key = getSessionKey(userId, sessionId);
        log.info("Deleting session key: {} from Redis", key);
        redisTemplate.delete(key);
    }

    @Override
    public void deleteAllSessions(UUID userId) {
        String pattern = getUserSessionsPattern(userId);
        log.info("Deleting all sessions for pattern: {} from Redis", pattern);
        
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    @Override
    public boolean hasSession(UUID userId, UUID sessionId) {
        String key = getSessionKey(userId, sessionId);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
