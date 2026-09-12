package com.example.TaskAPI.task.stream;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

public class EmitterHandle {
    private final SseEmitter emitter;
    private final ReentrantLock lock = new ReentrantLock();

    public EmitterHandle(SseEmitter emitter) {
        this.emitter = emitter;
    }

    public void send(String eventName, Object data) throws IOException {
        lock.lock();

        try {
            emitter.send(SseEmitter.event().name(eventName).data(data));
        } finally {
            lock.unlock();
        }
    }

    public void sendComment(String comment) throws IOException {
        lock.lock();

        try {
            emitter.send(SseEmitter.event().comment(comment));
        } finally {
            lock.unlock();
        }
    }

    public void complete() {
        lock.lock();

        try {
            emitter.complete();
        } finally {
            lock.unlock();
        }
    }
}
