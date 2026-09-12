package com.example.TaskAPI.task.api;

import com.example.TaskAPI.security.SecurityUtils;
import com.example.TaskAPI.task.stream.EmitterHandle;
import com.example.TaskAPI.task.stream.SseEmitterRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

@Tag(name = "Tasks", description = "Endpoints for managing tasks")
@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskStreamController {
    private static final long STREAM_TIMEOUT_MS = Duration.ofMinutes(30).toMillis();
    private final SseEmitterRegistry emitterRegistry;

    @Operation(summary = "Stream live dashboard events for the authenticated user")
    @GetMapping(path = "/stream", produces = "text/event-stream")
    public SseEmitter stream() {
        Long userId = SecurityUtils.getCurrentUserId();
        SseEmitter emitter = new SseEmitter(STREAM_TIMEOUT_MS);
        EmitterHandle handle = new EmitterHandle(emitter);

        emitter.onCompletion(() -> emitterRegistry.remove(userId, handle));
        emitter.onTimeout(() -> {
            emitterRegistry.remove(userId, handle);
            emitter.complete();
        });
        emitter.onError(ex -> emitterRegistry.remove(userId, handle));
        emitterRegistry.register(userId, handle);

        try {
            handle.send("connected", Map.of());
        } catch (IOException ex) {
            emitterRegistry.remove(userId, handle);
            emitter.completeWithError(ex);
        }

        return emitter;
    }
}
