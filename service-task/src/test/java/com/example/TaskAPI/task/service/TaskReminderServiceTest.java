package com.example.TaskAPI.task.service;

import com.example.TaskAPI.infrastructure.config.AppProperties;
import com.example.TaskAPI.infrastructure.config.TestAppProperties;
import com.example.TaskAPI.task.api.dto.TaskReminderResult;
import com.example.TaskAPI.task.domain.entity.Task;
import com.example.TaskAPI.task.domain.entity.TaskAssignee;
import com.example.TaskAPI.task.domain.event.TaskReminderScheduledEvent;
import com.example.TaskAPI.task.domain.repository.TaskRepository;
import com.example.TaskAPI.user.domain.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TaskReminderServiceTest {
    @InjectMocks
    private TaskReminderService taskReminderService;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Spy
    private AppProperties appProperties = TestAppProperties.defaults();

    private UUID taskUuid;

    @BeforeEach
    void setUp() {
        taskUuid = UUID.randomUUID();
    }

    private Task buildTask(String status, boolean withAssignee) {
        Set<TaskAssignee> assignees = withAssignee ?
                new HashSet<>(Set.of(TaskAssignee.builder()
                        .user(User.builder()
                                .uuid(UUID.randomUUID())
                                .build())
                        .build()))
                : new HashSet<>();

        return Task.builder()
                .uuid(taskUuid)
                .status(status)
                .taskAssignees(assignees)
                .build();
    }

    @Test
    void sendReminder_taskNotFound_returnsTaskNotFound() {
        when(taskRepository.findWithAssigneesByUuid(taskUuid))
                .thenReturn(Optional.empty());

        assertThat(taskReminderService.sendReminder(taskUuid))
                .isEqualTo(TaskReminderResult.TASK_NOT_FOUND);
    }

    @Test
    void sendReminder_completed_returnsSuppressedCompleted() {
        when(taskRepository.findWithAssigneesByUuid(taskUuid))
                .thenReturn(Optional.of(buildTask("COMPLETED", true)));

        assertThat(taskReminderService.sendReminder(taskUuid))
                .isEqualTo(TaskReminderResult.SUPPRESSED_COMPLETED);
    }

    @Test
    void sendReminder_noAssignee_returnsSuppressedNoAssignee() {
        when(taskRepository.findWithAssigneesByUuid(taskUuid))
                .thenReturn(Optional.of(buildTask("IN_PROGRESS", false)));

        assertThat(taskReminderService.sendReminder(taskUuid))
                .isEqualTo(TaskReminderResult.SUPPRESSED_NO_ASSIGNEE);
    }

    @Test
    void sendReminder_inProgressWithAssignee_returnSent() {
        when(taskRepository.findWithAssigneesByUuid(taskUuid))
                .thenReturn(Optional.of(buildTask("IN_PROGRESS", true)));

        assertThat(taskReminderService.sendReminder(taskUuid))
                .isEqualTo(TaskReminderResult.SENT);
    }

    @Test
    void onDueDateChanged_futureDate_publishedWithRemindAt() {
        LocalDate dueDate = LocalDate.now().plusDays(1);

        taskReminderService.onDueDateChanged(taskUuid, null, dueDate);

        verify(eventPublisher).publishEvent(new TaskReminderScheduledEvent(
                taskUuid,
                dueDate.atTime(taskReminderService.getSchedulerReminderTime())
                        .atZone(appProperties.timezone())
                        .toInstant()));
    }

    @Test
    void onDueDateChanged_unchanged_doesNotPublish() {
        LocalDate dueDate = LocalDate.now();

        taskReminderService.onDueDateChanged(taskUuid, dueDate, dueDate);

        verify(eventPublisher, never()).publishEvent(any(TaskReminderScheduledEvent.class));
    }

    @Test
    void onDueDateChanged_pastDate_publishesNullRemindAt() {
        LocalDate dueDate = LocalDate.now().minusDays(1);

        taskReminderService.onDueDateChanged(taskUuid, null, dueDate);

        verify(eventPublisher).publishEvent(new TaskReminderScheduledEvent(
                taskUuid,
                null));
    }

    @Test
    void onDueDateChanged_cleared_publishesNullRemindAt() {
        taskReminderService.onDueDateChanged(taskUuid, LocalDate.now(), null);

        verify(eventPublisher).publishEvent(new TaskReminderScheduledEvent(taskUuid, null));
    }

    @Test
    void cancel_publishesNullRemindAt() {
        taskReminderService.cancel(taskUuid);

        verify(eventPublisher).publishEvent(new TaskReminderScheduledEvent(taskUuid, null));
    }
}
