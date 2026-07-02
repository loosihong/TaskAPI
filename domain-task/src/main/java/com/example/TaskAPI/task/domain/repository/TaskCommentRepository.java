package com.example.TaskAPI.task.domain.repository;

import com.example.TaskAPI.core.model.repository.BaseEntityRepository;
import com.example.TaskAPI.task.domain.entity.TaskComment;
import com.example.TaskAPI.task.domain.query.TaskCommentListItem;

import java.util.List;
import java.util.UUID;

public interface TaskCommentRepository extends BaseEntityRepository<TaskComment> {
    List<TaskCommentListItem> findTaskCommentsListItemsByTaskUuid(UUID taskUuid);
}
