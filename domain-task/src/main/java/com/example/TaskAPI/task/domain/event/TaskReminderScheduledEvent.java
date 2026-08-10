package com.example.TaskAPI.task.domain.event;

import java.time.Instant;
import java.util.UUID;

public record TaskReminderScheduledEvent(
        UUID taskUuid,
        Instant remindAt
) {
}
