package com.example.TaskAPI.task.service;

import com.example.TaskAPI.core.exception.DataValidationException;
import com.example.TaskAPI.core.exception.EntityNotFoundException;
import com.example.TaskAPI.infrastructure.security.SecurityUtils;
import com.example.TaskAPI.task.domain.entity.Task;
import com.example.TaskAPI.task.domain.entity.TaskDetail;
import com.example.TaskAPI.task.domain.query.TaskDashboardFilter;
import com.example.TaskAPI.task.domain.query.TaskDashboardItem;
import com.example.TaskAPI.task.domain.query.TaskListFilter;
import com.example.TaskAPI.task.domain.query.TaskPredicateBuilder;
import com.example.TaskAPI.task.domain.repository.TaskRepository;
import com.example.TaskAPI.task.mapper.TaskMapper;
import com.example.TaskAPI.user.domain.entity.User;
import com.example.TaskAPI.user.domain.repository.UserRepository;
import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final UserRepository userRepository;
    private final TaskPredicateBuilder taskPredicateBuilder;

    public TaskService(
            TaskRepository taskRepository,
            TaskMapper taskMapper,
            UserRepository userRepository,
            TaskPredicateBuilder taskPredicateBuilder) {
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
        this.userRepository = userRepository;
        this.taskPredicateBuilder = taskPredicateBuilder;
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public Optional<Task> getTaskByUuid(UUID uuid) {
        return taskRepository.findByUuid(uuid);
    }

    @Transactional(readOnly = true)
    public Optional<Task> getWithDetailByUuid(UUID uuid) {
        return taskRepository.findWithDetailByUuid(uuid);
    }

    public Page<Task> searchTaskList(TaskListFilter filter, Pageable pageable) {
        Predicate predicate = taskPredicateBuilder.buildPredicate(filter);
        return taskRepository.findAll(predicate, pageable);
    }

    public Page<TaskDashboardItem> searchTaskDashboard(TaskDashboardFilter filter, Pageable pageable) {
        return taskRepository.findTaskDashboardItems(filter, pageable, SecurityUtils.getCurrentUserId());
    }

    @Transactional
    public Task createTask(Task task, TaskDetail taskDetail, Set<UUID> assigneeUuids) {
        if (task == null) {
            throw new DataValidationException(Task.class, "Task cannot be null.");
        }

        syncTaskAssignees(task, assigneeUuids);

        if (taskDetail != null) {
            task.setTaskDetail(taskDetail);
        }

        return taskRepository.save(task);
    }

    @Transactional
    public Task updateTask(UUID uuid, Task task, Set<UUID> assigneeUuids) {
        Task found = taskRepository.findWithDetailByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException(Task.class, uuid));

        syncTaskAssignees(task, assigneeUuids);
        taskMapper.update(task, found);

        return taskRepository.save(found);
    }

    @Transactional
    public TaskDetail updateTaskDetail(UUID taskUuid, TaskDetail taskDetail) {
        Task task = taskRepository.findWithDetailByUuid(taskUuid)
                .orElseThrow(() -> new EntityNotFoundException(Task.class, taskUuid));
        TaskDetail found = Optional.ofNullable(task.getTaskDetail())
                .orElseThrow(() -> new EntityNotFoundException(TaskDetail.class, taskUuid));

        taskMapper.update(taskDetail, found);

        return found;
    }

    public void deleteTask(UUID uuid) {
        if (!taskRepository.existsByUuid(uuid)) {
            throw new EntityNotFoundException(Task.class, uuid);
        }

        taskRepository.deleteByUuid(uuid);
    }

    public Task updateTaskAssignees(UUID taskUuid, Set<UUID> assigneeUuids) {
        Task task = taskRepository.findWithAssigneesByUuid(taskUuid)
                .orElseThrow(() -> new EntityNotFoundException(Task.class, taskUuid));

        syncTaskAssignees(task, assigneeUuids);

        return taskRepository.save(task);
    }

    private void syncTaskAssignees(Task task, Set<UUID> assigneeUuids) {
        if (task == null ||
                assigneeUuids == null) {
            return;
        }

        Set<User> users = (assigneeUuids.isEmpty() ? new HashSet<>()
                : new HashSet<>(userRepository.findAllByUuidIn(assigneeUuids)));

        if (users.size() != assigneeUuids.size()) {
            throw new DataValidationException(User.class,
                    "One or more assignee UUIDs are invalid or do not exits.");
        }

        task.syncAssignees(users);
    }
}