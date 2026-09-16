package com.example.TaskAPI.infrastructure.config;

import com.example.TaskAPI.redis.RedisPubSubConfig;
import com.example.TaskAPI.redis.RedisSubscription;
import com.example.TaskAPI.task.stream.SseEmitterRegistry;
import com.example.TaskAPI.task.stream.TaskChangeSubscriber;
import com.example.TaskAPI.task.stream.TaskEventChannel;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.ObjectMapper;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "taskapi.stream", name = "broadcaster", havingValue = "redis")
@Import(RedisPubSubConfig.class)
public class TaskStreamRedisConfig {
    @Bean
    public TaskChangeSubscriber taskChangeSubscriber(
            ObjectMapper objectMapper,
            SseEmitterRegistry emitterRegistry,
            MeterRegistry meterRegistry
    ) {
        return new TaskChangeSubscriber(objectMapper, emitterRegistry, meterRegistry);
    }

    @Bean
    public RedisSubscription taskChangeSubscription(TaskChangeSubscriber taskChangeSubscriber) {
        return new RedisSubscription(TaskEventChannel.NAME, taskChangeSubscriber);
    }
}
