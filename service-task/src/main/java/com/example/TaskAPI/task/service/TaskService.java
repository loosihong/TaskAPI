package com.example.TaskAPI.task.service;

import com.example.TaskAPI.core.exception.DataValidationException;
import com.example.TaskAPI.core.exception.EntityNotFoundException;
import com.example.TaskAPI.security.SecurityUtils;
import com.example.TaskAPI.task.domain.entity.Task;
import com.example.TaskAPI.task.domain.entity.TaskDetail;
import com.example.TaskAPI.task.domain.event.TaskChangeType;
import com.example.TaskAPI.task.domain.event.TaskChangedEvent;
import com.example.TaskAPI.task.domain.query.TaskDashboardFilter;
import com.example.TaskAPI.task.domain.query.TaskDashboardItem;
import com.example.TaskAPI.task.domain.query.TaskListFilter;
import com.example.TaskAPI.task.domain.query.TaskPredicateBuilder;
import com.example.TaskAPI.task.domain.repository.TaskRepository;
import com.example.TaskAPI.task.mapper.TaskMapper;
import com.example.TaskAPI.user.domain.entity.User;
import com.example.TaskAPI.user.domain.repository.UserRepository;
import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final UserRepository userRepository;
    private final TaskPredicateBuilder taskPredicateBuilder;
    private final TaskReminderService taskReminderService;
    private final ApplicationEventPublisher eventPublisher;

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Task> getWithAssigneesByUuid(UUID uuid) {
        return taskRepository.findWithAssigneesByUuid(uuid);
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

        Task saved = taskRepository.save(task);

        if (taskDetail != null) {
            taskReminderService.onDueDateChanged(saved.getUuid(), null, saved.getTaskDetail().getDueDate());
        }

        eventPublisher.publishEvent(
                new TaskChangedEvent(saved.getUuid(), TaskChangeType.CREATED, assigneeUserIds(saved)));

        return saved;
    }

    @Transactional
    public Task updateTask(UUID uuid, Task task, Set<UUID> assigneeUuids) {
        Task found = taskRepository.findWithDetailByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException(Task.class, uuid));
        LocalDate oldDueDate = Optional.ofNullable(found.getTaskDetail())
                .map(TaskDetail::getDueDate)
                .orElse(null);
        Set<Long> oldAssigneeIds = assigneeUserIds(found);

        syncTaskAssignees(task, assigneeUuids);
        taskMapper.update(task, found);
        taskReminderService.onDueDateChanged(
                found.getUuid(),
                oldDueDate,
                Optional.ofNullable(found.getTaskDetail())
                        .map(TaskDetail::getDueDate)
                        .orElse(null));

        Task saved = taskRepository.save(found);
        Set<Long> recipients = new HashSet<>(oldAssigneeIds);

        recipients.addAll(assigneeUserIds(saved));
        eventPublisher.publishEvent(new TaskChangedEvent(saved.getUuid(), TaskChangeType.UPDATED, recipients));

        return saved;
    }

    @Transactional
    public TaskDetail updateTaskDetail(UUID taskUuid, TaskDetail taskDetail) {
        Task task = taskRepository.findWithDetailByUuid(taskUuid)
                .orElseThrow(() -> new EntityNotFoundException(Task.class, taskUuid));
        TaskDetail found = Optional.ofNullable(task.getTaskDetail())
                .orElseThrow(() -> new EntityNotFoundException(TaskDetail.class, taskUuid));
        LocalDate oldDueDate = found.getDueDate();

        taskMapper.update(taskDetail, found);
        taskReminderService.onDueDateChanged(taskUuid, oldDueDate, found.getDueDate());

        eventPublisher.publishEvent(
                new TaskChangedEvent(taskUuid, TaskChangeType.UPDATED, assigneeUserIds(task)));

        return found;
    }

    @Transactional
    public void deleteTask(UUID uuid) {
        Task task = taskRepository.findWithAssigneesByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException(Task.class, uuid));
        Set<Long> recipients = assigneeUserIds(task);

        taskReminderService.cancel(uuid);
        taskRepository.deleteByUuid(uuid);

        eventPublisher.publishEvent(new TaskChangedEvent(uuid, TaskChangeType.DELETED, recipients));
    }

    @Transactional
    public Task updateTaskAssignees(UUID taskUuid, Set<UUID> assigneeUuids) {
        Task task = taskRepository.findWithAssigneesByUuid(taskUuid)
                .orElseThrow(() -> new EntityNotFoundException(Task.class, taskUuid));
        Set<Long> oldAssigneeIds = assigneeUserIds(task);

        syncTaskAssignees(task, assigneeUuids);

        Task saved = taskRepository.save(task);
        Set<Long> recipients = new HashSet<>(oldAssigneeIds);

        recipients.addAll(assigneeUserIds(saved));
        eventPublisher.publishEvent(new TaskChangedEvent(taskUuid, TaskChangeType.UPDATED, recipients));

        return saved;
    }

    private void syncTaskAssignees(Task task, Set<UUID> assigneeUuids) {
        if (task == null ||
                assigneeUuids == null) {
            return;
        }

        Set<User> users = assigneeUuids.isEmpty() ? new HashSet<>()
                : new HashSet<>(userRepository.findAllByUuidIn(assigneeUuids));

        if (users.size() != assigneeUuids.size()) {
            throw new DataValidationException(User.class,
                    "One or more assignee UUIDs are invalid or do not exits.");
        }

        task.syncAssignees(users);
    }

    private Set<Long> assigneeUserIds(Task task) {
        return task.getTaskAssignees().stream()
                .map(taskAssignee -> taskAssignee.getUser().getId())
                .collect(Collectors.toSet());
    }
}