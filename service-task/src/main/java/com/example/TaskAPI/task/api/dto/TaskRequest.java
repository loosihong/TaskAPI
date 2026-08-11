package com.example.TaskAPI.task.api.dto;

import com.example.TaskAPI.core.dto.BaseEntityDetailRequest;
import com.example.TaskAPI.task.domain.entity.Task;
import com.example.TaskAPI.task.domain.enums.TaskStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public interface TaskRequest {
    @Builder
    record Detail(
            @Size(max = Task.Constraints.Values.TITLE_MAX, message = Task.Constraints.Messages.TITLE_MAX)
            @NotBlank(message = Task.Constraints.Messages.TITLE_REQUIRED)
            String title,
            @NotNull(message = Task.Constraints.Messages.STATUS_REQUIRED)
            TaskStatus status,
            Set<UUID> assigneeUuids,
            @Valid
            TaskDetailRequest.Detail taskDetail,
            UUID uuid,
            Integer version
    ) implements BaseEntityDetailRequest {
        public Detail {
            assigneeUuids = (assigneeUuids == null ? new HashSet<>() : assigneeUuids);
        }
    }
}

