package com.example.TaskAPI.task.domain.query;

import lombok.Builder;

@Builder
public record TaskCommentListItem(
        String comment
) {
}
