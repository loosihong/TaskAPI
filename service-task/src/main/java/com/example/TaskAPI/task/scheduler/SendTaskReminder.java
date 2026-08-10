package com.example.TaskAPI.task.scheduler;

import org.jobrunr.jobs.lambdas.JobRequest;

import java.util.UUID;

public record SendTaskReminder(UUID taskUuid) implements JobRequest {
    @Override
    public Class<SendTaskReminderHandler> getJobRequestHandler() {
        return SendTaskReminderHandler.class;
    }
}
