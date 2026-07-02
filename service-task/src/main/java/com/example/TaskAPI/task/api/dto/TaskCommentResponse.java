package com.example.TaskAPI.task.api.dto;

import com.example.TaskAPI.core.dto.BaseEntityDetailResponse;
import lombok.Builder;

import java.util.UUID;

public interface TaskCommentResponse {
    @Builder
    record Detail(
            String comment,
            UUID uuid,
            Integer version
    ) implements BaseEntityDetailResponse {
    }
}