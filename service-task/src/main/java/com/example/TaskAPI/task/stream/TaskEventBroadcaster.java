package com.example.TaskAPI.task.stream;

import com.example.TaskAPI.task.domain.event.TaskChangedEvent;

public interface TaskEventBroadcaster {
    void broadcast(TaskChangedEvent event);
}
