package com.example.TaskAPI.task.scheduler;

import org.jobrunr.jobs.lambdas.JobRequest;

public record SweepOverdueTasks() implements JobRequest {
    @Override
    public Class<SweepOverdueTasksHandler> getJobRequestHandler() {
        return SweepOverdueTasksHandler.class;
    }
}
