package com.example.TaskAPI.task.scheduler;

import com.example.TaskAPI.task.domain.event.TaskReminderScheduledEvent;
import org.jobrunr.scheduling.JobRequestScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class SendTaskReminderEnqueuerTest {
    @Mock
    private JobRequestScheduler jobRequestScheduler;
    @InjectMocks
    private SendTaskReminderEnqueuer sendTaskReminderEnqueuer;

    private UUID taskUuid;
    private UUID expectedJobId;

    @BeforeEach
    void setUp() {
        taskUuid = UUID.randomUUID();
        expectedJobId = UUID.nameUUIDFromBytes(
                (SendTaskReminderEnqueuer.JOB_ID + taskUuid).getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void onTaskReminderScheduled_withRemindAt_deletesThenSchedules() {
        Instant remindAt = Instant.now().plusSeconds(3600);

        sendTaskReminderEnqueuer.onTaskReminderScheduled(new TaskReminderScheduledEvent(taskUuid, remindAt));

        InOrder inOrder = inOrder(jobRequestScheduler);
        inOrder.verify(jobRequestScheduler).delete(eq(expectedJobId), anyString());
        inOrder.verify(jobRequestScheduler).schedule(
                expectedJobId,
                remindAt,
                new SendTaskReminder(taskUuid));
    }

    @Test
    void onTaskReminderScheduled_noRemindAt_deletesOnly() {
        sendTaskReminderEnqueuer.onTaskReminderScheduled(new TaskReminderScheduledEvent(taskUuid, null));

        verify(jobRequestScheduler).delete(eq(expectedJobId), anyString());
        verify(jobRequestScheduler, never()).schedule(any(UUID.class), any(Instant.class), any(SendTaskReminder.class));
    }
}
