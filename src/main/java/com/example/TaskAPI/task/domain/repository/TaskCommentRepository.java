package com.example.TaskAPI.task.domain.repository;

import com.example.TaskAPI.core.model.repository.BaseEntityRepository;
import com.example.TaskAPI.task.api.dto.TaskCommentResponse;
import com.example.TaskAPI.task.domain.entity.TaskComment;

import java.util.List;
import java.util.UUID;

public interface TaskCommentRepository extends BaseEntityRepository<TaskComment> {
    List<TaskCommentResponse.ListItem> findTaskCommentsListItemsByTaskUuid(UUID taskUuid);
}
