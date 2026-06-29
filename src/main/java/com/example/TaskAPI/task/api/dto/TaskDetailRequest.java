package com.example.TaskAPI.task.api.dto;

import com.example.TaskAPI.core.dto.BaseExtensionEntityDetailRequest;
import com.example.TaskAPI.task.domain.entity.TaskDetail;
import com.example.TaskAPI.task.domain.enums.Priority;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.time.LocalDate;

public interface TaskDetailRequest {
    @Builder
    record Detail(
            @Size(max = TaskDetail.Constraints.Values.DESCRIPTION_MAX, message = TaskDetail.Constraints.Messages.DESCRIPTION_MAX)
            String description,
            @Future(message = TaskDetail.Constraints.Messages.DUE_DATE_FUTURE)
            LocalDate dueDate,
            @NotNull(message = TaskDetail.Constraints.Messages.PRIORITY_REQUIRED)
            Priority priority,
            Integer version
    ) implements BaseExtensionEntityDetailRequest {
    }
}

