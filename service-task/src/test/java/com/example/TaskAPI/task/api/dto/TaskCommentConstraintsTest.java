package com.example.TaskAPI.task.api.dto;

import com.example.TaskAPI.core.BaseConstraintsTest;
import com.example.TaskAPI.task.domain.entity.TaskComment;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class TaskCommentConstraintsTest extends BaseConstraintsTest {
    private static Stream<Arguments> happyCases() {
        return Stream.of(
                // comment
                Arguments.of(TaskCommentRequest.Detail.builder()
                                .comment("")
                                .build(),
                        TaskComment.Fields.comment),
                Arguments.of(TaskCommentRequest.Detail.builder()
                                .comment("a".repeat(TaskComment.Constraints.Values.COMMENT_MAX))
                                .build(),
                        TaskComment.Fields.comment)
        );
    }

    private static Stream<Arguments> violationCases() {
        return Stream.of(
                // comment
                Arguments.of(TaskCommentRequest.Detail.builder()
                                .comment("a".repeat(TaskComment.Constraints.Values.COMMENT_MAX + 1)).build(),
                        TaskComment.Fields.comment,
                        TaskComment.Constraints.Messages.COMMENT_MAX)
        );
    }

    @ParameterizedTest(name = HAPPY_CASE_MESSAGE)
    @MethodSource("happyCases")
    void taskCommentRequest_noError(TaskCommentRequest.Detail taskCommentRequest, String fieldName) {
        assertNoViolation(validator.validate(taskCommentRequest), fieldName);
    }

    @ParameterizedTest(name = VIOLATION_CASE_MESSAGE)
    @MethodSource("violationCases")
    void taskCommentRequest_error(TaskCommentRequest.Detail taskCommentRequest, String fieldName, String expectedError) {
        assertViolation(validator.validate(taskCommentRequest), fieldName, expectedError);
    }
}
