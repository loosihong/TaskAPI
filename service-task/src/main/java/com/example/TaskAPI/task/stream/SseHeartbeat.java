package com.example.TaskAPI.task.stream;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class SseHeartbeat {
    private static final long HEARTBEAT_MS = 20_000L;
    private final SseEmitterRegistry emitterRegistry;

    @Scheduled(fixedRate = HEARTBEAT_MS)
    public void sendHeartbeat() {
        for (EmitterHandle handle : emitterRegistry.all()) {
            try {
                handle.sendComment("ping");
            } catch (IOException ex) {
                handle.complete();
            }
        }
    }
}
