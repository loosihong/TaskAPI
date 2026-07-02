package com.example.TaskAPI.task.domain.query;

import com.example.TaskAPI.task.domain.enums.Priority;
import lombok.experimental.FieldNameConstants;

import java.time.LocalDateTime;
import java.util.UUID;

@FieldNameConstants
public record TaskDashboardItem(
        UUID taskUuid,
        String title,
        String status,
        LocalDateTime createdAt,
        String createdByName,
        LocalDateTime updatedAt,
        String updatedByName,
        Priority priority,
        String assigneeNames
) {
}
