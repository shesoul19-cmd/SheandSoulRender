package com.sheandsoul.v1update.config;

// New Imports
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;

import java.time.Duration;

@Configuration
public class RedisCacheConfig {

    /**
     * This bean creates a properly configured JSON serializer.
     * It registers the JavaTimeModule to correctly handle LocalDateTime,
     * and enables default typing for robust serialization.
     */
    @Bean
    public GenericJackson2JsonRedisSerializer jackson2JsonRedisSerializer() {
        // This validator is needed for security with default typing
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator
                .builder()
                .allowIfBaseType(Object.class)
                .build();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // <-- This line fixes the error
        objectMapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL);

        return new GenericJackson2JsonRedisSerializer(objectMapper);
    }

    /**
     * This bean defines the default cache configuration, now using
     * our properly configured JSON serializer.
     */
    @Bean
    public RedisCacheConfiguration cacheConfiguration(GenericJackson2JsonRedisSerializer jackson2JsonRedisSerializer) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues()
                // Use our new, configured serializer
                .serializeValuesWith(SerializationPair.fromSerializer(jackson2JsonRedisSerializer));
    }

    /**
     * This bean applies custom TTLs to specific caches, also using
     * our new, configured serializer.
     */
    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer(GenericJackson2JsonRedisSerializer jackson2JsonRedisSerializer) {
        return (builder) -> builder
                .withCacheConfiguration("article",
                        RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofHours(1))
                                .disableCachingNullValues()
                                .serializeValuesWith(SerializationPair.fromSerializer(jackson2JsonRedisSerializer)))
                .withCacheConfiguration("musicList",
                        RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofDays(1))
                                .disableCachingNullValues()
                                .serializeValuesWith(SerializationPair.fromSerializer(jackson2JsonRedisSerializer)))
                .withCacheConfiguration("userNotes", // We keep the config from earlier
                        RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofMinutes(30))
                                .disableCachingNullValues()
                                .serializeValuesWith(SerializationPair.fromSerializer(jackson2JsonRedisSerializer)));
    }
}