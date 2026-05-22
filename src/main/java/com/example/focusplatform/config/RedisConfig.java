package com.example.focusplatform.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 1. Create a custom Jackson ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();

        // 2. CRITICAL FIX: Register the JavaTimeModule to handle LocalDateTime
        objectMapper.registerModule(new JavaTimeModule());

        // 3. Format dates as clean ISO-8601 strings (e.g., "2026-05-21T19:22:16") instead of numeric arrays
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 4. Remember the exact Java class types so Spring can reconstruct them when reading from the cache
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        // 5. Pass our custom mapper into the Redis Serializer
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        // Apply serializers to the template
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(jsonSerializer); // Use custom serializer here

        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(jsonSerializer); // And here

        template.afterPropertiesSet();
        return template;
    }
}