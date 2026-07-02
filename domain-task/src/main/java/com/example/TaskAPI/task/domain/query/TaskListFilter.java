package com.example.TaskAPI.task.domain.query;

import lombok.Builder;
import lombok.experimental.FieldNameConstants;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@FieldNameConstants
@Builder
public record TaskListFilter(
        String title,
        List<String> statuses,
        List<UUID> assigneeUuids,
        LocalDateTime createdAtFrom,
        LocalDateTime createdAtTo
) {
}
