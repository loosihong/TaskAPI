package com.example.TaskAPI.task.api.dto;

import com.example.TaskAPI.core.dto.PageableRequest;
import jakarta.validation.constraints.NotNull;

public record TaskListSearchRequest(
        @NotNull TaskListFilter filter,
        PageableRequest pageable
) {
    public PageableRequest pageable() {
        return pageable != null ? pageable : PageableRequest.builder().build();
    }
}
