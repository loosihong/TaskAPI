package com.example.TaskAPI.task.scheduler;

import com.example.TaskAPI.infrastructure.config.AppProperties;
import com.example.TaskAPI.infrastructure.config.TestAppProperties;
import com.example.TaskAPI.task.batch.SweepOverdueTasksJobConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.test.MetaDataInstanceFactory;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SweepOverdueTasksHandlerTest {
    @Spy
    private AppProperties appProperties = TestAppProperties.defaults();
    @Mock
    private JobOperator jobOperator;
    @Mock
    private Job sweepOverdueTasksJob;
    @InjectMocks
    private SweepOverdueTasksHandler handler;

    @Test
    void startsJobWithTodayAsCutOffDate() throws Exception {
        when(jobOperator.start(eq(sweepOverdueTasksJob), any()))
                .thenReturn(MetaDataInstanceFactory.createJobExecution());

        handler.run(new SweepOverdueTasks());

        ArgumentCaptor<JobParameters> captor = ArgumentCaptor.forClass(JobParameters.class);
        verify(jobOperator).start(eq(sweepOverdueTasksJob), captor.capture());
        assertThat(captor.getValue().getLocalDate(SweepOverdueTasksJobConfig.PARAM_CUT_OFF_DATE))
                .isEqualTo(LocalDate.now(appProperties.timezone()));
    }
}
