package com.example.TaskAPI.task.api;

import com.example.TaskAPI.task.api.dto.TaskCommentRequest;
import com.example.TaskAPI.task.api.dto.TaskCommentResponse;
import com.example.TaskAPI.task.mapper.TaskCommentMapper;
import com.example.TaskAPI.task.service.TaskCommentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Task Comments")
@RestController
@RequestMapping("/comments")
public class TaskCommentController {
    private final TaskCommentService taskCommentService;
    private final TaskCommentMapper taskCommentMapper;

    public TaskCommentController(TaskCommentService taskCommentService, TaskCommentMapper taskCommentMapper) {
        this.taskCommentService = taskCommentService;
        this.taskCommentMapper = taskCommentMapper;
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<TaskCommentResponse.Detail> updateTaskComment(
            @PathVariable UUID uuid,
            @Valid @RequestBody TaskCommentRequest.Detail taskCommentRequest) {
        return ResponseEntity.ok(
                taskCommentMapper.toResponse(
                        taskCommentService.updateTaskComment(
                                uuid,
                                taskCommentMapper.toEntity(taskCommentRequest))));
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> deleteTaskComment(
            @PathVariable UUID uuid) {
        taskCommentService.deleteTaskComment(uuid);

        return ResponseEntity.noContent().build();
    }
}
