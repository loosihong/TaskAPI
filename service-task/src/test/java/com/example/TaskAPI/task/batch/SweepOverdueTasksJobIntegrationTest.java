package com.example.TaskAPI.task.batch;

import com.example.TaskAPI.task.domain.entity.Task;
import com.example.TaskAPI.task.domain.entity.TaskDetail;
import com.example.TaskAPI.task.domain.enums.Priority;
import com.example.TaskAPI.task.domain.enums.TaskStatus;
import com.example.TaskAPI.task.domain.repository.TaskRepository;
import com.example.TaskAPI.web.BaseIntegrationTest;
import org.jobrunr.scheduling.JobRequestScheduler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.InvalidJobParametersException;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.test.JobOperatorTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBatchTest
public class SweepOverdueTasksJobIntegrationTest extends BaseIntegrationTest {
    private static final LocalDate DUE_BEFORE_CUT_OFF = LocalDate.now().plusDays(1);
    private static final LocalDate DUE_AFTER_CUT_OFF = LocalDate.now().plusDays(90);
    private static final int BATCH_SIZE = 5;

    @MockitoBean
    private JobRequestScheduler jobRequestScheduler;
    @Autowired
    private JobOperatorTestUtils jobOperatorTestUtils;
    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;
    @Autowired
    private TaskRepository taskRepository;

    @AfterEach
    void clearBatchMetadata() {
        jobRepositoryTestUtils.removeJobExecutions();
    }

    @Test
    void sweepsOnlyQualifyingTasks() throws Exception {
        UUID overdue1 = createTask(TaskStatus.TODO, DUE_BEFORE_CUT_OFF).getUuid();
        UUID overdue2 = createTask(TaskStatus.TODO, DUE_BEFORE_CUT_OFF).getUuid();
        UUID overdue3 = createTask(TaskStatus.TODO, DUE_BEFORE_CUT_OFF).getUuid();
        UUID completed = createTask(TaskStatus.DONE, DUE_BEFORE_CUT_OFF).getUuid();
        UUID notDue = createTask(TaskStatus.TODO, DUE_AFTER_CUT_OFF).getUuid();

        JobExecution jobExecution = jobOperatorTestUtils.startJob(
                jobParameters(LocalDate.now().plusDays(30)));

        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(writeCountOf(jobExecution)).isEqualTo(3);
        assertThat(statusOf(overdue1)).isEqualTo(TaskStatus.OVERDUE);
        assertThat(statusOf(overdue2)).isEqualTo(TaskStatus.OVERDUE);
        assertThat(statusOf(overdue3)).isEqualTo(TaskStatus.OVERDUE);
        assertThat(statusOf(completed)).isEqualTo(TaskStatus.DONE);
        assertThat(statusOf(notDue)).isEqualTo(TaskStatus.TODO);
    }

    @Test
    void sweepsAcrossMultipleInvocations() throws Exception {
        int taskCount = 3 * BATCH_SIZE;
        List<UUID> taskUuids = new ArrayList<>();

        for (int i = 0; i < taskCount; i++) {
            taskUuids.add(createTask(TaskStatus.TODO, DUE_BEFORE_CUT_OFF).getUuid());
        }

        JobExecution jobExecution = jobOperatorTestUtils.startJob(
                jobParameters(LocalDate.now().plusDays(31)));

        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(writeCountOf(jobExecution)).isEqualTo(taskCount);
        assertThat(taskUuids).allSatisfy(uuid ->
                assertThat(statusOf(uuid)).isEqualTo(TaskStatus.OVERDUE));
    }

    @Test
    void noQualifyingTasks_completesWithNoWrites() throws Exception {
        UUID notDue = createTask(TaskStatus.TODO, DUE_AFTER_CUT_OFF).getUuid();

        JobExecution jobExecution = jobOperatorTestUtils.startJob(
                jobParameters(LocalDate.now().plusDays(32)));

        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(writeCountOf(jobExecution)).isZero();
        assertThat(statusOf(notDue)).isEqualTo(TaskStatus.TODO);
    }

    @Test
    void missingCutOffDate_throws() {
        assertThatThrownBy(() -> jobOperatorTestUtils.startJob(new JobParameters()))
                .isInstanceOf(InvalidJobParametersException.class);
    }

    private Task createTask(TaskStatus status, LocalDate dueDate) {
        Task task = Task.builder()
                .title("task")
                .status(status)
                .build();
        TaskDetail detail = TaskDetail.builder()
                .dueDate(dueDate)
                .priority(Priority.LOW)
                .build();

        task.setTaskDetail(detail);

        return taskRepository.save(task);
    }

    private JobParameters jobParameters(LocalDate cutOffDate) {
        return new JobParametersBuilder()
                .addLocalDate(SweepOverdueTasksJobConfig.PARAM_CUT_OFF_DATE, cutOffDate)
                .toJobParameters();
    }

    private long writeCountOf(JobExecution jobExecution) {
        return jobExecution.getStepExecutions().iterator().next().getWriteCount();
    }

    private TaskStatus statusOf(UUID uuid) {
        return taskRepository.findByUuid(uuid).orElseThrow().getStatus();
    }
}
