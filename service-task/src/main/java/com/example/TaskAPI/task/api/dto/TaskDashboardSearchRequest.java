package com.example.TaskAPI.task.api.dto;

import com.example.TaskAPI.core.dto.PageableRequest;
import com.example.TaskAPI.task.domain.query.TaskDashboardFilter;
import jakarta.validation.constraints.NotNull;

public record TaskDashboardSearchRequest(
        @NotNull TaskDashboardFilter filter,
        PageableRequest pageable
) {
    public PageableRequest pageable() {
        return pageable != null ? pageable : PageableRequest.builder().build();
    }
}
