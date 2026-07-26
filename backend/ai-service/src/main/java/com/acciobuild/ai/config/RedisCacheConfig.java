package com.acciobuild.ai.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Enterprise Redis caching configuration with dynamic JVM in-memory fallback.
 * Prevents unit test failures when Redis is offline.
 */
@Configuration
@EnableCaching
@Slf4j
public class RedisCacheConfig {

    @Bean
    @Primary
    public CacheManager cacheManager(ObjectProvider<RedisConnectionFactory> connectionFactoryProvider) {
        RedisConnectionFactory connectionFactory = connectionFactoryProvider.getIfAvailable();
        if (connectionFactory == null) {
            log.warn("RedisConnectionFactory bean is not available. Falling back to in-memory ConcurrentMapCacheManager.");
            return new ConcurrentMapCacheManager(
                    "promptTemplates", "activePromptVersions", "memories", "promptVariables",
                    "providerMetadata", "modelMetadata", "providerCapabilities", "providerConfigurations",
                    "executionPlans", "retrievedKnowledge", "toolMetadata", "rankingResults"
            );
        }

        try {
            RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(Duration.ofMinutes(10))
                    .disableCachingNullValues()
                    .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

            Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
            cacheConfigurations.put("promptTemplates", defaultCacheConfig.entryTtl(Duration.ofMinutes(15)));
            cacheConfigurations.put("activePromptVersions", defaultCacheConfig.entryTtl(Duration.ofMinutes(5)));
            cacheConfigurations.put("memories", defaultCacheConfig.entryTtl(Duration.ofMinutes(5)));
            cacheConfigurations.put("promptVariables", defaultCacheConfig.entryTtl(Duration.ofMinutes(30)));
            cacheConfigurations.put("providerMetadata", defaultCacheConfig.entryTtl(Duration.ofHours(1)));
            cacheConfigurations.put("modelMetadata", defaultCacheConfig.entryTtl(Duration.ofHours(1)));
            cacheConfigurations.put("providerCapabilities", defaultCacheConfig.entryTtl(Duration.ofHours(24)));
            cacheConfigurations.put("providerConfigurations", defaultCacheConfig.entryTtl(Duration.ofMinutes(10)));
            cacheConfigurations.put("executionPlans", defaultCacheConfig.entryTtl(Duration.ofMinutes(5)));
            cacheConfigurations.put("retrievedKnowledge", defaultCacheConfig.entryTtl(Duration.ofMinutes(5)));
            cacheConfigurations.put("toolMetadata", defaultCacheConfig.entryTtl(Duration.ofHours(1)));
            cacheConfigurations.put("rankingResults", defaultCacheConfig.entryTtl(Duration.ofMinutes(5)));

            return RedisCacheManager.builder(connectionFactory)
                    .cacheDefaults(defaultCacheConfig)
                    .withInitialCacheConfigurations(cacheConfigurations)
                    .build();
        } catch (Exception e) {
            log.error("Failed to initialize RedisCacheManager. Falling back to in-memory ConcurrentMapCacheManager.", e);
            return new ConcurrentMapCacheManager(
                    "promptTemplates", "activePromptVersions", "memories", "promptVariables",
                    "providerMetadata", "modelMetadata", "providerCapabilities", "providerConfigurations",
                    "executionPlans", "retrievedKnowledge", "toolMetadata", "rankingResults"
            );
        }
    }
}
