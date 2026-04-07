package io.github.darlene.leakdetectionapplication.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import io.github.darlene.leakdetectionapplication.dto.response.MLPredictionResponse;

/**
 * Redis configuration.
 * Configures RedisTemplate with JSON serialization for
 * MLPredictionResponse objects stored in cache.
 */
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, MLPredictionResponse> redisTemplate(
            RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, MLPredictionResponse> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }
}