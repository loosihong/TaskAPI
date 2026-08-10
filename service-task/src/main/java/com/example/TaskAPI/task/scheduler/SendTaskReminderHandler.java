package com.example.TaskAPI.task.scheduler;

import com.example.TaskAPI.task.api.dto.TaskReminderResult;
import com.example.TaskAPI.task.service.TaskReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.jobs.lambdas.JobRequestHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SendTaskReminderHandler implements JobRequestHandler<SendTaskReminder> {
    private final TaskReminderService taskReminderService;

    @Override
    @Job(name = "Reminder for task %0")
    public void run(SendTaskReminder request) {
        TaskReminderResult result = taskReminderService.sendReminder(request.taskUuid());
        log.info("Task {} reminder outcome: {}", request.taskUuid(), result);
    }
}
