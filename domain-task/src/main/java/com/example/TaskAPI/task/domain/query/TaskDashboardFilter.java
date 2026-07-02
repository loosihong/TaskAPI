package com.example.TaskAPI.task.domain.query;

import com.example.TaskAPI.task.domain.enums.Priority;
import lombok.Builder;
import lombok.experimental.FieldNameConstants;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@FieldNameConstants
@Builder
public record TaskDashboardFilter(
        String title,
        List<String> statuses,
        List<Priority> priorities,
        LocalDateTime updatedAtFrom,
        LocalDateTime updatedAtTo,
        List<UUID> updatedByUuids
) {
}
