package com.example.TaskAPI.task.stream;

import com.example.TaskAPI.task.domain.event.TaskChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "taskapi.stream", name = "broadcaster", havingValue = "redis")
@RequiredArgsConstructor
public class RedisTaskEventBroadcaster implements TaskEventBroadcaster {
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void broadcast(TaskChangedEvent event) {
        try {
            redisTemplate.convertAndSend(TaskEventChannel.NAME, objectMapper.writeValueAsString(event));
        } catch (RuntimeException ex) { // NOPMD - AvoidCatchingGenericException
            log.warn("Failed to publish task-change event to Redis: {}", event, ex);
        }
    }
}
