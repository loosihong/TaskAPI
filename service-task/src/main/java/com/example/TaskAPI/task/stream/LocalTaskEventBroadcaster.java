package com.example.TaskAPI.task.stream;

import com.example.TaskAPI.task.domain.event.TaskChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "taskapi.stream", name = "broadcaster", havingValue = "local", matchIfMissing = true)
@RequiredArgsConstructor
public class LocalTaskEventBroadcaster implements TaskEventBroadcaster {
    private final SseEmitterRegistry emitterRegistry;

    @Override
    public void broadcast(TaskChangedEvent event) {
        emitterRegistry.send(event.recipientUserIds(), event);
    }
}
