package com.example.TaskAPI.task.mapper;

import com.example.TaskAPI.core.BaseMapperTest;
import com.example.TaskAPI.task.api.dto.TaskCommentRequest;
import com.example.TaskAPI.task.api.dto.TaskCommentResponse;
import com.example.TaskAPI.task.domain.entity.TaskComment;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

public class TaskCommentMapperTest extends BaseMapperTest {
    private final TaskCommentMapper taskCommentMapper = Mappers.getMapper(TaskCommentMapper.class);

    @Test
    void toEntity_mapsTaskCommentRequest() {
        TaskCommentRequest.Detail taskCommentRequest = TaskCommentRequest.Detail.builder()
                .uuid(UUID.randomUUID())
                .comment("North London forever")
                .version(1)
                .build();
        TaskComment expectedTaskComment = TaskComment.builder()
                .uuid(taskCommentRequest.uuid())
                .comment(taskCommentRequest.comment())
                .version(taskCommentRequest.version())
                .build();
        TaskComment actualTaskComment = taskCommentMapper.toEntity(taskCommentRequest);

        assertThatMappedEntity(actualTaskComment)
                .isEqualTo(expectedTaskComment);
    }

    @Test
    void toResponse_mapsTaskComment() {
        TaskComment taskComment = TaskComment.builder()
                .uuid(UUID.randomUUID())
                .comment("North London forever")
                .version(1)
                .taskId(1L)
                .build();
        TaskCommentResponse.Detail expectedTaskCommentResponse = TaskCommentResponse.Detail
                .builder()
                .uuid(taskComment.getUuid())
                .comment(taskComment.getComment())
                .version(taskComment.getVersion())
                .build();
        TaskCommentResponse.Detail actualTaskCommentResponse = taskCommentMapper.toResponse(taskComment);

        assertThatMappedEntity(actualTaskCommentResponse)
                .isEqualTo(expectedTaskCommentResponse);
    }
}
