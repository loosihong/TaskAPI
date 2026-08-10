package com.example.TaskAPI.task.batch;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.test.MetaDataInstanceFactory;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
public class SweepOverdueTasksTaskletTest {
    @Mock
    private JPAQueryFactory jpaQueryFactory;
    @InjectMocks
    private SweepOverdueTasksTasklet tasklet;

    @Test
    void execute_missingCutOffDate_throws() {
        StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution(new JobParameters());
        ChunkContext chunkContext = new ChunkContext(new StepContext(stepExecution));

        assertThatThrownBy(() -> tasklet.execute(stepExecution.createStepContribution(), chunkContext))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(SweepOverdueTasksJobConfig.PARAM_CUT_OFF_DATE);
        verifyNoInteractions(jpaQueryFactory);
    }
}
