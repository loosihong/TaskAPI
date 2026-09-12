package com.example.TaskAPI.task.stream;

import com.example.TaskAPI.task.domain.event.TaskChangeType;
import com.example.TaskAPI.task.domain.event.TaskChangedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class SseEmitterRegistryTest {
    private SseEmitterRegistry emitterRegistry;

    private static TaskChangedEvent anyEvent(Long userId) {
        return new TaskChangedEvent(UUID.randomUUID(), TaskChangeType.UPDATED, Set.of(userId));
    }

    @BeforeEach
    void setup() {
        emitterRegistry = new SseEmitterRegistry();
    }

    @Nested
    @DisplayName("register / remove")
    class RegisterAndRemove {
        @Test
        void send_multipleEmittersForSameUser_removingOneStillDeliversToOther() throws IOException {
            Long userId = 1L;
            EmitterHandle removedHandle = mock(EmitterHandle.class);
            EmitterHandle remainingHandle = mock(EmitterHandle.class);
            TaskChangedEvent event = anyEvent(userId);

            emitterRegistry.register(userId, removedHandle);
            emitterRegistry.register(userId, remainingHandle);
            emitterRegistry.remove(userId, removedHandle);
            emitterRegistry.send(Set.of(userId), event);

            verify(removedHandle, never()).send(anyString(), any());
            verify(remainingHandle).send("task-changed", event);
        }

        @Test
        void remove_lastHandleForUser_removesUserKeyEntirely() {
            Long userId = 1L;
            EmitterHandle handle = mock(EmitterHandle.class);

            emitterRegistry.register(userId, handle);
            emitterRegistry.remove(userId, handle);

            assertThat(emitterRegistry.count()).isZero();
            assertThat(emitterRegistry.userCount()).isZero();
        }
    }

    @Nested
    @DisplayName("send")
    class Send {
        @Test
        void send_deadClientThrowsIOException_removesOnlyThatHandleAndDeliversToOthers() throws IOException {
            Long userId = 1L;
            EmitterHandle deadHandle = mock(EmitterHandle.class);
            EmitterHandle liveHandle = mock(EmitterHandle.class);
            TaskChangedEvent event = anyEvent(userId);

            doThrow(new IOException("broken pipe"))
                    .when(deadHandle).send(anyString(), any());

            emitterRegistry.register(userId, deadHandle);
            emitterRegistry.register(userId, liveHandle);
            emitterRegistry.send(Set.of(userId), event);

            verify(deadHandle).complete();
            verify(liveHandle).send("task-changed", event);
            assertThat(emitterRegistry.count()).isOne();
        }
    }

    @Nested
    @DisplayName("concurrency")
    class Concurrency {
        private static void awaitStart(CountDownLatch start) {
            try {
                start.await();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }

        @Test
        void concurrentRegisterAndRemove_sameUser_noLostUpdatesOrExceptions() throws Exception {
            Long userId = 1L;
            int existingCount = 100;
            int newCount = 100;
            List<EmitterHandle> existingHandles = IntStream.range(0, existingCount)
                    .mapToObj(i -> mock(EmitterHandle.class))
                    .toList();
            List<EmitterHandle> newHandles = IntStream.range(0, newCount)
                    .mapToObj(i -> mock(EmitterHandle.class))
                    .toList();
            ExecutorService executor = Executors.newFixedThreadPool(existingCount + newCount);
            CountDownLatch ready = new CountDownLatch(existingCount + newCount);
            CountDownLatch start = new CountDownLatch(1);
            List<Future<?>> futures = new ArrayList<>();

            existingHandles.forEach(handle -> emitterRegistry.register(userId, handle));

            for (EmitterHandle handle : existingHandles) {
                futures.add(executor.submit(() -> {
                    ready.countDown();
                    awaitStart(start);
                    emitterRegistry.remove(userId, handle);
                }));
            }

            for (EmitterHandle handle : newHandles) {
                futures.add(executor.submit(() -> {
                    ready.countDown();
                    awaitStart(start);
                    emitterRegistry.register(userId, handle);
                }));
            }

            boolean allReady = ready.await(5, TimeUnit.SECONDS);
            assertThat(allReady).as("all threads reached the start line").isTrue();
            start.countDown();

            for (Future<?> future : futures) {
                future.get(5, TimeUnit.SECONDS);
            }

            executor.shutdown();

            assertThat(emitterRegistry.count()).isEqualTo(newCount);
            assertThat(emitterRegistry.userCount()).isOne();
        }
    }
}
