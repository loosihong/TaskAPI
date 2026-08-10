package com.example.TaskAPI.task.batch;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.parameters.DefaultJobParametersValidator;
import org.springframework.batch.core.job.parameters.JobParametersValidator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class SweepOverdueTasksJobConfig {
    public static final String JOB_NAME = "sweepOverdueTasks";
    public static final String PARAM_CUT_OFF_DATE = "cutOffDate";
    private static final String STEP_NAME = "sweepOverdueTasksStep";

    @Bean
    public Step sweepOverdueTasksStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            SweepOverdueTasksTasklet tasklet) {
        return new StepBuilder(STEP_NAME, jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }

    @Bean
    public Job sweepOverdueTasksJob(JobRepository jobRepository, Step sweepOverdueTasksStep) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .validator(jobParametersValidator())
                .start(sweepOverdueTasksStep)
                .build();
    }

    private JobParametersValidator jobParametersValidator() {
        return new DefaultJobParametersValidator(
                new String[] { PARAM_CUT_OFF_DATE },
                new String[] {});
    }
}
