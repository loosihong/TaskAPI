package com.example.TaskAPI.task.stream;

import com.example.TaskAPI.task.domain.event.TaskChangedEvent;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class TaskChangeSubscriber implements MessageListener {
    private final ObjectMapper objectMapper;
    private final SseEmitterRegistry emitterRegistry;
    private final Counter receivedCounter;

    public TaskChangeSubscriber(
            ObjectMapper objectMapper,
            SseEmitterRegistry emitterRegistry,
            MeterRegistry meterRegistry
    ) {
        this.objectMapper = objectMapper;
        this.emitterRegistry = emitterRegistry;
        this.receivedCounter = Counter.builder("taskapi.stream.received")
                .description("Envelopes received from Redis")
                .register(meterRegistry);
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String payload = new String(message.getBody(), StandardCharsets.UTF_8);
            TaskChangedEvent event = objectMapper.readValue(payload, TaskChangedEvent.class);

            emitterRegistry.send(event.recipientUserIds(), event);
            receivedCounter.increment();
        } catch (RuntimeException ex) { // NOPMD - AvoidCatchingGenericException
            log.warn("Failed to process task-changed message from Redis: payload = {}",
                    new String(message.getBody(), StandardCharsets.UTF_8), ex);
        }
    }
}
