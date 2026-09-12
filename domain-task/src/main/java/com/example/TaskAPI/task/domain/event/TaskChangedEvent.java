package com.example.TaskAPI.task.domain.event;

import java.util.Set;
import java.util.UUID;

public record TaskChangedEvent(
        UUID taskUuid,
        TaskChangeType changeType,
        Set<Long> recipientUserIds
) {
    public TaskChangedEvent {
        recipientUserIds = recipientUserIds == null ? Set.of() : Set.copyOf(recipientUserIds);
    }
}
