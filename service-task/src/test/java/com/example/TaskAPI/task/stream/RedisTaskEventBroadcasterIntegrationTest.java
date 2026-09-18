package com.example.TaskAPI.task.stream;

import com.example.TaskAPI.redis.RedisTestContainer;
import com.example.TaskAPI.task.domain.event.TaskChangeType;
import com.example.TaskAPI.task.domain.event.TaskChangedEvent;
import com.example.TaskAPI.web.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

public class RedisTaskEventBroadcasterIntegrationTest extends BaseIntegrationTest {
    @Autowired
    private TaskEventBroadcaster broadcaster;
    @Autowired
    private SseEmitterRegistry emitterRegistry;

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        RedisTestContainer.register(registry);
        registry.add("taskapi.stream.broadcaster", () -> "redis");
    }

    @Test
    void broadcast_publishesToRedis_subscriberDeliversToRegisteredEmitter() throws IOException {
        Long userId = loginUser.getId();
        EmitterHandle handle = mock(EmitterHandle.class);
        TaskChangedEvent event = new TaskChangedEvent(UUID.randomUUID(), TaskChangeType.UPDATED, Set.of(userId));

        emitterRegistry.register(userId, handle);

        try {
            await().atMost(Duration.ofSeconds(10))
                    .pollInterval(Duration.ofMillis(200))
                    .untilAsserted(() -> {
                        broadcaster.broadcast(event);

                        verify(handle, timeout(5000)).send(eq("task-changed"), eq(event));
                    });
        } finally {
            emitterRegistry.remove(userId, handle);
        }
    }
}
