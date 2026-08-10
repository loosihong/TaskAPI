package com.example.TaskAPI.task.scheduler;

import com.example.TaskAPI.infrastructure.config.AppProperties;
import lombok.RequiredArgsConstructor;
import org.jobrunr.scheduling.JobRequestScheduler;
import org.jobrunr.scheduling.cron.Cron;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SweepOverdueTasksEnqueuer {
    public static final String JOB_ID = "sweep-overdue-tasks";

    private final JobRequestScheduler scheduler;
    private final AppProperties appProperties;

    @EventListener(ApplicationReadyEvent.class)
    public void register() {
        scheduler.scheduleRecurrently(
                JOB_ID,
                Cron.daily(1),
                appProperties.timezone(),
                new SweepOverdueTasks());
    }
}
