package com.example.TaskAPI.task.api;

import com.example.TaskAPI.core.model.BaseEntity;
import com.example.TaskAPI.task.api.dto.*;
import com.example.TaskAPI.task.domain.query.TaskCommentListItem;
import com.example.TaskAPI.task.domain.query.TaskDashboardItem;
import com.example.TaskAPI.task.mapper.TaskCommentMapper;
import com.example.TaskAPI.task.mapper.TaskMapper;
import com.example.TaskAPI.task.service.TaskCommentService;
import com.example.TaskAPI.task.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static org.springframework.data.domain.Sort.Direction.DESC;

@Tag(name = "Tasks", description = "Endpoints for managing tasks")
@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;
    private final TaskMapper taskMapper;
    private final TaskCommentService taskCommentService;
    private final TaskCommentMapper taskCommentMapper;

    public TaskController(
            TaskService taskService,
            TaskMapper taskMapper,
            TaskCommentService taskCommentService,
            TaskCommentMapper taskCommentMapper) {
        this.taskService = taskService;
        this.taskMapper = taskMapper;
        this.taskCommentService = taskCommentService;
        this.taskCommentMapper = taskCommentMapper;
    }

    @Operation(summary = "Get all tasks", description = "Returns a list of all tasks")
    @GetMapping
    public ResponseEntity<List<TaskResponse.Detail>> getAllTasks() {
        return ResponseEntity.ok(
                taskMapper.toResponseList(taskService.getAllTasks()));
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<TaskResponse.Detail> getTask(
            @PathVariable UUID uuid) {
        return taskService.getWithDetailByUuid(uuid)
                .map(task -> ResponseEntity.ok(taskMapper.toResponse(task)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/listing")
    public ResponseEntity<Page<TaskResponse.Detail>> searchTaskList(
            @Valid @RequestBody TaskListSearchRequest searchRequest) {
        return ResponseEntity.ok(
                taskService.searchTaskList(
                                searchRequest.filter(),
                                searchRequest.pageable().toPageable(DESC, BaseEntity.Fields.createdAt))
                        .map(taskMapper::toResponse));
    }

    @PostMapping("/dashboard")
    public ResponseEntity<Page<TaskDashboardItem>> searchTaskDashboard(
            @Valid @RequestBody TaskDashboardSearchRequest searchRequest) {
        return ResponseEntity.ok(
                taskService.searchTaskDashboard(
                        searchRequest.filter(),
                        searchRequest.pageable().toPageable(DESC, TaskDashboardItem.Fields.updatedAt)));
    }

    @Operation(summary = "Create a task", description = "Creates a new task and returns it")
    @PostMapping
    public ResponseEntity<TaskResponse.Detail> createTask(
            @Valid @RequestBody TaskRequest.Detail taskRequest) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(taskMapper.toResponse(
                        taskService.createTask(
                                taskMapper.toEntity(taskRequest),
                                taskRequest.taskDetail() == null ?
                                        null : taskMapper.toEntity(taskRequest.taskDetail()),
                                taskRequest.assigneeUuids())));
    }

    @Operation(summary = "Update a task")
    @PutMapping("/{uuid}")
    public ResponseEntity<TaskResponse.Detail> updateTask(
            @PathVariable UUID uuid,
            @Valid @RequestBody TaskRequest.Detail taskRequest) {
        if (!uuid.equals(taskRequest.uuid())) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(
                taskMapper.toResponse(
                        taskService.updateTask(
                                uuid,
                                taskMapper.toEntity(taskRequest),
                                taskRequest.assigneeUuids())));
    }

    @Operation(summary = "Delete a task")
    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> deleteTask(@PathVariable UUID uuid) {
        taskService.deleteTask(uuid);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{uuid}/detail")
    public ResponseEntity<TaskDetailResponse.Detail> updateTaskDetail(
            @PathVariable UUID uuid,
            @Valid @RequestBody TaskDetailRequest.Detail taskDetailRequest) {
        return ResponseEntity.ok(
                taskMapper.toTaskDetailResponse(
                        taskService.updateTaskDetail(uuid, taskMapper.toEntity(taskDetailRequest))));
    }

    @GetMapping("/{taskUuid}/comments")
    public ResponseEntity<List<TaskCommentListItem>> getTaskCommentsListItemByTaskUuid(
            @PathVariable UUID taskUuid) {
        return ResponseEntity.ok(
                taskCommentService.getTaskCommentsListItemsByTaskUuid(taskUuid));
    }

    @PostMapping("/{taskUuid}/comments")
    public ResponseEntity<TaskCommentResponse.Detail> createTaskComment(
            @PathVariable UUID taskUuid,
            @Valid @RequestBody TaskCommentRequest.Detail taskCommentRequest) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(taskCommentMapper.toResponse(
                        taskCommentService.createTaskComment(
                                taskUuid,
                                taskCommentMapper.toEntity(taskCommentRequest))));
    }

    @PutMapping("/{taskUuid}/assignees")
    public ResponseEntity<TaskResponse.Detail> updateTaskAssignees(
            @PathVariable UUID taskUuid,
            @Valid @RequestBody TaskAssigneeRequest.Assign taskAssigneeRequest) {
        return ResponseEntity.ok(
                taskMapper.toResponse(
                        taskService.updateTaskAssignees(taskUuid, taskAssigneeRequest.assigneeUuids())));
    }
}
