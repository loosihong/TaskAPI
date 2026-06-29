package com.example.TaskAPI.task.api.dto;

import com.example.TaskAPI.core.dto.BaseEntityDetailRequest;
import com.example.TaskAPI.task.domain.entity.Task;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
            @Size(max = Task.Constraints.Values.STATUS_MAX, message = Task.Constraints.Messages.STATUS_MAX)
            @NotBlank(message = Task.Constraints.Messages.STATUS_REQUIRED)
            String status,
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

