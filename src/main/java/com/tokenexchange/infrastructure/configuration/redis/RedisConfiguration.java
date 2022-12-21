package com.tokenexchange.infrastructure.configuration.redis;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(RedisConfigurationProperties.class)
public class RedisConfiguration {
    @Bean
    public StatefulRedisConnection<String, String> redisConnection(RedisConfigurationProperties redisConfigurationProperties) {
        RedisURI redisURI = RedisURI.Builder.redis(redisConfigurationProperties.getHost())
                .withPassword(redisConfigurationProperties.getPassword())
                .withPort(redisConfigurationProperties.getPort())
                .withSsl(false)
                .withTimeout(Duration.ofSeconds(redisConfigurationProperties.getTimeout()))
                .withVerifyPeer(false)
                .withDatabase(0)
                .build();
        return RedisClient.create(redisURI)
                .connect();
    }
}
