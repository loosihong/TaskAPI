package com.example.TaskAPI.task.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public interface TaskAssigneeRequest {
    @Builder
    record Assign(
            @NotNull
            Set<UUID> assigneeUuids
    ) {
        public Assign {
            assigneeUuids = (assigneeUuids == null ? new HashSet<>() : assigneeUuids);
        }
    }
}
