package com.example.TaskAPI.task.service;

import com.example.TaskAPI.core.exception.DataValidationException;
import com.example.TaskAPI.core.exception.EntityNotFoundException;
import com.example.TaskAPI.task.api.dto.TaskCommentResponse;
import com.example.TaskAPI.task.domain.entity.Task;
import com.example.TaskAPI.task.domain.entity.TaskComment;
import com.example.TaskAPI.task.domain.repository.TaskCommentRepository;
import com.example.TaskAPI.task.domain.repository.TaskRepository;
import com.example.TaskAPI.task.mapper.TaskCommentMapper;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TaskCommentService {
    private final TaskRepository taskRepository;
    private final TaskCommentRepository taskCommentRepository;
    private final TaskCommentMapper taskCommentMapper;

    public TaskCommentService(
            TaskRepository taskRepository,
            TaskCommentRepository taskCommentRepository,
            TaskCommentMapper taskCommentMapper) {
        this.taskRepository = taskRepository;
        this.taskCommentRepository = taskCommentRepository;
        this.taskCommentMapper = taskCommentMapper;
    }

    public List<TaskCommentResponse.ListItem> getTaskCommentsListItemsByTaskUuid(UUID taskUuid) {
        return taskCommentRepository.findTaskCommentsListItemsByTaskUuid(taskUuid);
    }

    @Transactional
    public TaskComment createTaskComment(UUID taskUuid, TaskComment taskComment) {
        if (taskComment == null) {
            throw new DataValidationException(TaskComment.class, "TaskComment cannot be null.");
        }

        Task task = taskRepository.findByUuid(taskUuid)
                .orElseThrow(() -> new EntityNotFoundException(Task.class, taskUuid));
        task.addComment(taskComment);

        return taskCommentRepository.save(taskComment);
    }

    @Transactional
    public TaskComment updateTaskComment(UUID uuid, TaskComment taskComment) {
        TaskComment found = taskCommentRepository.findByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException(TaskComment.class, uuid));

        taskCommentMapper.update(taskComment, found);

        return taskCommentRepository.save(found);
    }

    public void deleteTaskComment(UUID uuid) {
        TaskComment taskComment = taskCommentRepository.findByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException(TaskComment.class, uuid));

        taskComment.getTask().removeComment(taskComment);
        taskCommentRepository.delete(taskComment);
    }
}
