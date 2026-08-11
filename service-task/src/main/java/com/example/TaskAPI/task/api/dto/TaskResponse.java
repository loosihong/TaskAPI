package com.example.TaskAPI.task.api.dto;

import com.example.TaskAPI.core.dto.BaseEntityDetailResponse;
import com.example.TaskAPI.task.domain.enums.TaskStatus;
import com.example.TaskAPI.user.api.dto.UserResponse;
import lombok.Builder;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public interface TaskResponse {
    @Builder
    record Detail(
            String title,
            TaskStatus status,
            TaskDetailResponse.Detail taskDetail,
            Set<UserResponse.Summary> taskAssignees,
            UUID uuid,
            Integer version
    ) implements BaseEntityDetailResponse {
        public Detail {
            taskAssignees = (taskAssignees == null ? new HashSet<>() : taskAssignees);
        }
    }
}
