package com.example.TaskAPI.task.batch;

import com.example.TaskAPI.task.domain.entity.QTask;
import com.example.TaskAPI.task.domain.entity.QTaskDetail;
import com.example.TaskAPI.task.domain.entity.Task;
import com.example.TaskAPI.task.domain.enums.TaskStatus;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SweepOverdueTasksTasklet implements Tasklet {
    private static final long MAX_SWEEP = 100_000L;

    private final JPAQueryFactory jpaQueryFactory;

    @Value("${spring.batch.custom.default-batch-size:100}")
    private int batchSize;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        LocalDate cutOffDate = chunkContext.getStepContext()
                .getStepExecution()
                .getJobParameters()
                .getLocalDate(SweepOverdueTasksJobConfig.PARAM_CUT_OFF_DATE);

        if (cutOffDate == null) {
            throw new IllegalArgumentException(
                    "Job parameter '" + SweepOverdueTasksJobConfig.PARAM_CUT_OFF_DATE + "' is required");
        }

        QTask qTask = QTask.task;
        QTaskDetail qTaskDetail = QTaskDetail.taskDetail;

        List<Task> taskBatch = jpaQueryFactory
                .select(qTask)
                .from(qTask)
                .innerJoin(qTaskDetail).on(qTaskDetail.task.id.eq(qTask.id))
                .where(qTaskDetail.dueDate.lt(cutOffDate)
                        .and(qTask.status.notIn(TaskStatus.OVERDUE, TaskStatus.DONE)))
                .limit(batchSize)
                .fetch();

        if (taskBatch.isEmpty()) {
            return RepeatStatus.FINISHED;
        }

        taskBatch.forEach(task -> task.setStatus(TaskStatus.OVERDUE));
        contribution.incrementWriteCount(taskBatch.size());
        log.debug("Swept {} overdue tasks", taskBatch.size());

        if (chunkContext.getStepContext().getStepExecution().getWriteCount() > MAX_SWEEP) {
            log.warn("Sweep exceeded {} rows, stopping early", MAX_SWEEP);
            return RepeatStatus.FINISHED;
        }

        return RepeatStatus.CONTINUABLE;
    }
}
