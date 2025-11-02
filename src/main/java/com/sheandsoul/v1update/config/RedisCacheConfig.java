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

    @Bean
    public GenericJackson2JsonRedisSerializer jackson2JsonRedisSerializer() {
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator
                .builder()
                .allowIfBaseType(Object.class)
                .build();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // <-- This line fixes the error
        objectMapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL);

        return new GenericJackson2JsonRedisSerializer(objectMapper);
    }

    @Bean
    public RedisCacheConfiguration cacheConfiguration(GenericJackson2JsonRedisSerializer jackson2JsonRedisSerializer) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues()
                .serializeValuesWith(SerializationPair.fromSerializer(jackson2JsonRedisSerializer));
    }

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
                .withCacheConfiguration("userNotes",
                        RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofMinutes(30))
                                .disableCachingNullValues()
                                .serializeValuesWith(SerializationPair.fromSerializer(jackson2JsonRedisSerializer)));
    }
}