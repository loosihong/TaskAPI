package com.example.TaskAPI.task.scheduler;

import com.example.TaskAPI.infrastructure.config.AppProperties;
import com.example.TaskAPI.task.batch.SweepOverdueTasksJobConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jobrunr.jobs.lambdas.JobRequestHandler;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class SweepOverdueTasksHandler implements JobRequestHandler<SweepOverdueTasks> {
    private final JobOperator jobOperator;
    private final Job sweepOverdueTasksJob;
    private final AppProperties appProperties;

    @Override
    @org.jobrunr.jobs.annotations.Job(name = "Sweep overdue tasks")
    public void run(SweepOverdueTasks request) throws Exception {
        JobParameters parameters = new JobParametersBuilder()
                .addLocalDate(SweepOverdueTasksJobConfig.PARAM_CUT_OFF_DATE, LocalDate.now(appProperties.timezone()))
                .toJobParameters();
        JobExecution execution = jobOperator.start(sweepOverdueTasksJob, parameters);

        log.info("{} finished: {}", SweepOverdueTasksJobConfig.JOB_NAME, execution.getStatus());
    }
}
