package com.example.TaskAPI.task.scheduler;

import com.example.TaskAPI.task.domain.event.TaskReminderScheduledEvent;
import lombok.RequiredArgsConstructor;
import org.jobrunr.scheduling.JobRequestScheduler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SendTaskReminderEnqueuer {
    public static final String JOB_ID = "send-task-reminder";

    private final JobRequestScheduler scheduler;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTaskReminderScheduled(TaskReminderScheduledEvent event) {
        UUID jobId = UUID.nameUUIDFromBytes((JOB_ID + event.taskUuid()).getBytes(StandardCharsets.UTF_8));
        scheduler.delete(jobId, "reminder rescheduled");

        if (event.remindAt() != null) {
            scheduler.schedule(
                    jobId,
                    event.remindAt(),
                    new SendTaskReminder(event.taskUuid()));
        }
    }
}
