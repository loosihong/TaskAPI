package com.example.TaskAPI.task.api.dto;

import com.example.TaskAPI.core.BaseConstraintsTest;
import com.example.TaskAPI.task.domain.entity.Task;
import com.example.TaskAPI.task.domain.entity.TaskDetail;
import com.example.TaskAPI.task.domain.enums.Priority;
import com.example.TaskAPI.task.domain.enums.TaskStatus;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.stream.Stream;

public class TaskConstraintsTest extends BaseConstraintsTest {
    private static Stream<Arguments> taskHappyCases() {
        return Stream.of(
                // title
                Arguments.of(TaskRequest.Detail.builder()
                                .title("a")
                                .build(),
                        Task.Fields.title),
                Arguments.of(TaskRequest.Detail.builder()
                                .title("a".repeat(Task.Constraints.Values.TITLE_MAX)).build(),
                        Task.Fields.title),

                // status
                Arguments.of(TaskRequest.Detail.builder()
                                .status(TaskStatus.TODO)
                                .build(),
                        Task.Fields.status),
                Arguments.of(TaskRequest.Detail.builder()
                                .status(TaskStatus.DONE)
                                .build(),
                        Task.Fields.status),
                Arguments.of(TaskRequest.Detail.builder()
                                .status(TaskStatus.IN_PROGRESS)
                                .build(),
                        Task.Fields.status),
                Arguments.of(TaskRequest.Detail.builder()
                                .status(TaskStatus.OVERDUE)
                                .build(),
                        Task.Fields.status),
                Arguments.of(TaskRequest.Detail.builder()
                                .status(TaskStatus.CANCELLED)
                                .build(),
                        Task.Fields.status)
        );
    }

    private static Stream<Arguments> taskViolationCases() {
        return Stream.of(
                // title
                Arguments.of(TaskRequest.Detail.builder()
                                .title("")
                                .build(),
                        Task.Fields.title,
                        Task.Constraints.Messages.TITLE_REQUIRED),
                Arguments.of(TaskRequest.Detail.builder()
                                .title(" ")
                                .build(),
                        Task.Fields.title,
                        Task.Constraints.Messages.TITLE_REQUIRED),
                Arguments.of(TaskRequest.Detail.builder()
                                .title("a".repeat(Task.Constraints.Values.TITLE_MAX + 1)).build(),
                        Task.Fields.title,
                        Task.Constraints.Messages.TITLE_MAX),

                // status
                Arguments.of(TaskRequest.Detail.builder()
                                .status(null)
                                .build(),
                        Task.Fields.status,
                        Task.Constraints.Messages.STATUS_REQUIRED)
        );
    }

    private static Stream<Arguments> taskDetailHappyCases() {
        return Stream.of(
                // description
                Arguments.of(TaskDetailRequest.Detail.builder()
                                .description("a".repeat(TaskDetail.Constraints.Values.DESCRIPTION_MAX))
                                .priority(Priority.LOW)
                                .build(),
                        TaskDetail.Fields.description),

                // priority
                Arguments.of(TaskDetailRequest.Detail.builder()
                                .priority(Priority.LOW)
                                .build(),
                        TaskDetail.Fields.priority),
                Arguments.of(TaskDetailRequest.Detail.builder()
                                .priority(Priority.MEDIUM)
                                .build(),
                        TaskDetail.Fields.priority),
                Arguments.of(TaskDetailRequest.Detail.builder()
                                .priority(Priority.HIGH)
                                .build(),
                        TaskDetail.Fields.priority),

                // dueDate
                Arguments.of(TaskDetailRequest.Detail.builder()
                                .dueDate(LocalDate.now().plusDays(1))
                                .priority(Priority.LOW)
                                .build(),
                        TaskDetail.Fields.dueDate)
        );
    }

    private static Stream<Arguments> taskDetailViolationCases() {
        return Stream.of(
                // description
                Arguments.of(TaskDetailRequest.Detail.builder()
                                .description("a".repeat(TaskDetail.Constraints.Values.DESCRIPTION_MAX + 1))
                                .priority(Priority.LOW)
                                .build(),
                        TaskDetail.Fields.description,
                        TaskDetail.Constraints.Messages.DESCRIPTION_MAX),

                // priority
                Arguments.of(TaskDetailRequest.Detail.builder()
                                .priority(null)
                                .build(),
                        TaskDetail.Fields.priority,
                        TaskDetail.Constraints.Messages.PRIORITY_REQUIRED),

                // dueDate
                Arguments.of(TaskDetailRequest.Detail.builder()
                                .dueDate(LocalDate.now().minusDays(1))
                                .priority(Priority.LOW)
                                .build(),
                        TaskDetail.Fields.dueDate,
                        TaskDetail.Constraints.Messages.DUE_DATE_FUTURE),
                Arguments.of(TaskDetailRequest.Detail.builder()
                                .dueDate(LocalDate.now())
                                .priority(Priority.LOW)
                                .build(),
                        TaskDetail.Fields.dueDate,
                        TaskDetail.Constraints.Messages.DUE_DATE_FUTURE)
        );
    }

    @ParameterizedTest(name = HAPPY_CASE_MESSAGE)
    @MethodSource("taskHappyCases")
    void taskRequest_noError(TaskRequest.Detail taskRequest, String fieldName) {
        assertNoViolation(validator.validate(taskRequest), fieldName);
    }

    @ParameterizedTest(name = VIOLATION_CASE_MESSAGE)
    @MethodSource("taskViolationCases")
    void taskRequest_showsError(TaskRequest.Detail taskRequest, String fieldName, String expectedError) {
        assertViolation(validator.validate(taskRequest), fieldName, expectedError);
    }

    @ParameterizedTest(name = HAPPY_CASE_MESSAGE)
    @MethodSource("taskDetailHappyCases")
    void taskDetailRequest_noError(TaskDetailRequest.Detail taskDetailRequest, String fieldName) {
        assertNoViolation(validator.validate(taskDetailRequest), fieldName);
    }

    @ParameterizedTest(name = VIOLATION_CASE_MESSAGE)
    @MethodSource("taskDetailViolationCases")
    void updateTaskDetail_violationCheck(TaskDetailRequest.Detail taskDetailRequest, String fieldName, String expectedError) {
        assertViolation(validator.validate(taskDetailRequest), fieldName, expectedError);
    }
}
