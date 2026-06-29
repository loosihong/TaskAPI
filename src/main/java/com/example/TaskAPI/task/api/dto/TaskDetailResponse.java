package com.example.TaskAPI.task.api.dto;

import com.example.TaskAPI.core.dto.BaseExtensionEntityDetailResponse;
import com.example.TaskAPI.task.domain.enums.Priority;
import lombok.Builder;

import java.time.LocalDate;

public interface TaskDetailResponse {
    @Builder
    record Detail(
            String description,
            LocalDate dueDate,
            Priority priority,
            Integer version
    ) implements BaseExtensionEntityDetailResponse {
    }
}

