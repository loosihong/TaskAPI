package com.example.TaskAPI.task.service;

import com.example.TaskAPI.infrastructure.config.AppProperties;
import com.example.TaskAPI.task.api.dto.TaskReminderResult;
import com.example.TaskAPI.task.domain.entity.Task;
import com.example.TaskAPI.task.domain.event.TaskReminderScheduledEvent;
import com.example.TaskAPI.task.domain.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskReminderService {
    private static final LocalTime DEFAULT_SCHEDULER_REMINDER_TIME = LocalTime.of(9, 0);
    private static final String STATUS_COMPLETED = "COMPLETED";
    private final TaskRepository taskRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final AppProperties appProperties;

    public TaskReminderResult sendReminder(UUID taskUuid) {
        Task task = taskRepository.findWithAssigneesByUuid(taskUuid).orElse(null);

        if (task == null) {
            return TaskReminderResult.TASK_NOT_FOUND;
        }

        if (STATUS_COMPLETED.equals(task.getStatus())) {
            return TaskReminderResult.SUPPRESSED_COMPLETED;
        }

        if (task.getTaskAssignees().isEmpty()) {
            return TaskReminderResult.SUPPRESSED_NO_ASSIGNEE;
        }

        return TaskReminderResult.SENT;
    }

    public void onDueDateChanged(UUID taskUuid, LocalDate oldDueDate, LocalDate newDueDate) {
        if (Objects.equals(oldDueDate, newDueDate)) {
            return;
        }

        Instant remindAt = (newDueDate == null || newDueDate.isBefore(LocalDate.now()))
                ? null
                : newDueDate.atTime(getSchedulerReminderTime()).atZone(appProperties.timezone()).toInstant();

        eventPublisher.publishEvent(new TaskReminderScheduledEvent(taskUuid, remindAt));
    }

    public void cancel(UUID taskUuid) {
        eventPublisher.publishEvent(new TaskReminderScheduledEvent(taskUuid, null));
    }

    public LocalTime getSchedulerReminderTime() {
        return DEFAULT_SCHEDULER_REMINDER_TIME;
    }
}
