package com.example.TaskAPI.task.api.dto;

import com.example.TaskAPI.core.dto.BaseEntityDetailRequest;
import com.example.TaskAPI.task.domain.entity.TaskComment;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.UUID;

public interface TaskCommentRequest {
    @Builder
    record Detail(
            @Size(max = TaskComment.Constraints.Values.COMMENT_MAX, message = TaskComment.Constraints.Messages.COMMENT_MAX)
            String comment,
            UUID uuid,
            Integer version
    ) implements BaseEntityDetailRequest {
    }
}

