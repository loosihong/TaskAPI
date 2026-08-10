package com.example.TaskAPI.task.scheduler;

import com.example.TaskAPI.infrastructure.config.AppProperties;
import com.example.TaskAPI.infrastructure.config.TestAppProperties;
import org.jobrunr.scheduling.JobRequestScheduler;
import org.jobrunr.scheduling.cron.Cron;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZoneId;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class SweepOverdueTasksEnqueuerTest {
    @Spy
    private AppProperties appProperties = TestAppProperties.defaults();
    @Mock
    private JobRequestScheduler scheduler;
    @InjectMocks
    private SweepOverdueTasksEnqueuer enqueuer;

    @Test
    void registersDailyRecurringSweepInConfiguredZone() {
        ZoneId expectedZone = appProperties.timezone();

        enqueuer.register();

        verify(scheduler).scheduleRecurrently(
                eq(SweepOverdueTasksEnqueuer.JOB_ID),
                eq(Cron.daily(1)),
                eq(expectedZone),
                any(SweepOverdueTasks.class));
    }
}
