package com.example.TaskAPI.task.api;

import com.example.TaskAPI.task.api.dto.TaskCommentRequest;
import com.example.TaskAPI.task.api.dto.TaskCommentResponse;
import com.example.TaskAPI.task.domain.query.TaskCommentListItem;
import com.example.TaskAPI.task.mapper.TaskCommentMapper;
import com.example.TaskAPI.task.service.TaskCommentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Task Comments")
@RestController
public class TaskCommentController {
    private final TaskCommentService taskCommentService;
    private final TaskCommentMapper taskCommentMapper;

    public TaskCommentController(TaskCommentService taskCommentService, TaskCommentMapper taskCommentMapper) {
        this.taskCommentService = taskCommentService;
        this.taskCommentMapper = taskCommentMapper;
    }

    @GetMapping("/tasks/{taskUuid}/comments")
    public ResponseEntity<List<TaskCommentListItem>> getTaskCommentsListItemByTaskUuid(
            @PathVariable UUID taskUuid) {
        return ResponseEntity.ok(
                taskCommentService.getTaskCommentsListItemsByTaskUuid(taskUuid));
    }

    @PostMapping("/tasks/{taskUuid}/comments")
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

    @PutMapping("/comments/{uuid}")
    public ResponseEntity<TaskCommentResponse.Detail> updateTaskComment(
            @PathVariable UUID uuid,
            @Valid @RequestBody TaskCommentRequest.Detail taskCommentRequest) {
        return ResponseEntity.ok(
                taskCommentMapper.toResponse(
                        taskCommentService.updateTaskComment(
                                uuid,
                                taskCommentMapper.toEntity(taskCommentRequest))));
    }

    @DeleteMapping("/comments/{uuid}")
    public ResponseEntity<Void> deleteTaskComment(
            @PathVariable UUID uuid) {
        taskCommentService.deleteTaskComment(uuid);

        return ResponseEntity.noContent().build();
    }
}
