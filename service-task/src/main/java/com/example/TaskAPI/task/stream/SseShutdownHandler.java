package com.example.TaskAPI.task.stream;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SseShutdownHandler {
    private final SseEmitterRegistry emitterRegistry;

    @EventListener(ContextClosedEvent.class)
    public void drain() {
        emitterRegistry.all().forEach(EmitterHandle::complete);
    }
}
