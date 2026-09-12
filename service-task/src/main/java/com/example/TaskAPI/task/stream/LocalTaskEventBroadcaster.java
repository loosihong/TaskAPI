package com.example.TaskAPI.task.stream;

import com.example.TaskAPI.task.domain.event.TaskChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LocalTaskEventBroadcaster implements TaskEventBroadcaster {
    private final SseEmitterRegistry emitterRegistry;

    @Override
    public void broadcast(TaskChangedEvent event) {
        emitterRegistry.send(event.recipientUserIds(), event);
    }
}
