package com.example.TaskAPI.task.stream;

import com.example.TaskAPI.task.domain.event.TaskChangedEvent;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class SseEmitterRegistry {
    private final ConcurrentMap<Long, Set<EmitterHandle>> emittersByUserId = new ConcurrentHashMap<>();

    public void register(Long userId, EmitterHandle handle) {
        Set<EmitterHandle> handles;

        do {
            handles = emittersByUserId.computeIfAbsent(userId, id -> ConcurrentHashMap.newKeySet());
            handles.add(handle);
        } while (!emittersByUserId.get(userId).equals(handles));
    }

    public void remove(Long userId, EmitterHandle handle) {
        emittersByUserId.computeIfPresent(userId, (id, handles) -> {
            handles.remove(handle);

            return handles.isEmpty() ? null : handles;
        });
    }

    public void send(Collection<Long> userIds, TaskChangedEvent event) {
        for (Long userId : userIds) {
            Set<EmitterHandle> handles = emittersByUserId.get(userId);
            if (handles == null || handles.isEmpty()) {
                continue;
            }

            for (EmitterHandle handle : Set.copyOf(handles)) {
                try {
                    handle.send("task-changed", event);
                } catch (IOException ex) {
                    handle.complete();
                    remove(userId, handle);
                }
            }
        }
    }

    public Collection<EmitterHandle> all() {
        return emittersByUserId.values().stream()
                .flatMap(Set::stream)
                .toList();
    }

    public int count() {
        return emittersByUserId.values().stream()
                .mapToInt(Set::size)
                .sum();
    }

    int userCount() {
        return emittersByUserId.size();
    }
}
