package com.example.TaskAPI.task.stream;

import com.example.TaskAPI.task.domain.event.TaskChangedEvent;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "taskapi.stream", name = "broadcaster", havingValue = "redis")
public class RedisTaskEventBroadcaster implements TaskEventBroadcaster {
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final Counter publishedCounter;
    private final Counter publishFailedCounter;

    public RedisTaskEventBroadcaster(
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            MeterRegistry meterRegistry
    ) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.publishedCounter = Counter.builder("taskapi.stream.published")
                .description("Envelopes published to Redis")
                .register(meterRegistry);
        this.publishFailedCounter = Counter.builder("taskapi.stream.publish.failed")
                .description("Publish attempts that threw before reaching Redis")
                .register(meterRegistry);
    }

    @Override
    public void broadcast(TaskChangedEvent event) {
        try {
            redisTemplate.convertAndSend(TaskEventChannel.NAME, objectMapper.writeValueAsString(event));
            publishedCounter.increment();
        } catch (RuntimeException ex) { // NOPMD - AvoidCatchingGenericException
            log.warn("Failed to publish task-change event to Redis: {}", event, ex);
            publishFailedCounter.increment();
        }
    }
}
