package com.example.TaskAPI.task.api;

import com.example.TaskAPI.core.exception.EntityNotFoundException;
import com.example.TaskAPI.task.api.dto.TaskCommentRequest;
import com.example.TaskAPI.task.api.dto.TaskCommentResponse;
import com.example.TaskAPI.task.domain.entity.Task;
import com.example.TaskAPI.task.domain.entity.TaskComment;
import com.example.TaskAPI.task.domain.query.TaskCommentListItem;
import com.example.TaskAPI.task.mapper.TaskCommentMapper;
import com.example.TaskAPI.task.service.TaskCommentService;
import com.example.TaskAPI.web.BaseControllerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskCommentController.class)
public class TaskCommentControllerTest extends BaseControllerTest {
    @MockitoBean
    private TaskCommentService taskCommentService;

    @MockitoBean
    private TaskCommentMapper taskCommentMapper;

    private TaskCommentResponse.Detail getTaskCommentResponse(UUID uuid) {
        return TaskCommentResponse.Detail.builder()
                .uuid(uuid)
                .build();
    }

    private TaskCommentRequest.Detail getTaskCommentRequest(UUID uuid) {
        return TaskCommentRequest.Detail.builder()
                .uuid(uuid)
                .build();
    }

    @Nested
    @DisplayName("/tasks/comments")
    class TaskCommentTest {
        UUID uuid;
        UUID taskUuid;

        @BeforeEach
        void setUp() {
            uuid = UUID.randomUUID();
            taskUuid = UUID.randomUUID();
        }

        @Test
        void getTaskCommentListItemsByTaskUuid_Found_returnsTaskComments() throws Exception {
            when(taskCommentService.getTaskCommentsListItemsByTaskUuid(taskUuid))
                    .thenReturn(List.of(
                            TaskCommentListItem.builder().build(),
                            TaskCommentListItem.builder().build()));

            mockMvc.perform(get("/tasks/{taskUuid}/comments", taskUuid)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));
        }

        @Test
        void getTaskCommentListItemsByTaskUuid_NotFound_returns404() throws Exception {
            when(taskCommentService.getTaskCommentsListItemsByTaskUuid(taskUuid))
                    .thenThrow(new EntityNotFoundException(Task.class, taskUuid));

            mockMvc.perform(get("/tasks/{taskUuid}/comments", taskUuid))
                    .andExpect(status().isNotFound());
        }

        @Test
        void createTaskComment_foundTask_returnsTaskComment() throws Exception {
            when(taskCommentService.createTaskComment(eq(taskUuid), any(TaskComment.class)))
                    .thenReturn(new TaskComment());
            when(taskCommentMapper.toEntity(any(TaskCommentRequest.Detail.class)))
                    .thenReturn(new TaskComment());
            when(taskCommentMapper.toResponse(any(TaskComment.class)))
                    .thenReturn(getTaskCommentResponse(uuid));

            mockMvc.perform(post("/tasks/{taskUuid}/comments", taskUuid)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(getTaskCommentRequest(uuid))))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.uuid").value(uuid.toString()));
        }

        @Test
        void createTaskComment_invalid_returnsBadRequest() throws Exception {
            mockMvc.perform(post("/tasks/{taskUuid}/comments", UUID.randomUUID())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    TaskCommentRequest.Detail.builder()
                                            .comment("a".repeat(2048))
                                            .build())))
                    .andExpect(status().isBadRequest());
        }

        private TaskCommentRequest.Detail getTaskCommentRequest(UUID uuid) {
            return TaskCommentRequest.Detail.builder()
                    .uuid(uuid)
                    .build();
        }

        private TaskCommentResponse.Detail getTaskCommentResponse(UUID uuid) {
            return TaskCommentResponse.Detail.builder()
                    .uuid(uuid)
                    .build();
        }
    }

    @Nested
    @DisplayName("PUT /comments")
    class UpdateTaskComment {
        @Test
        void updateTaskComment_found_returnsTaskComment() throws Exception {
            UUID uuid = UUID.randomUUID();

            when(taskCommentService.updateTaskComment(any(UUID.class), any(TaskComment.class)))
                    .thenReturn(new TaskComment());
            when(taskCommentMapper.toEntity(any(TaskCommentRequest.Detail.class)))
                    .thenReturn(new TaskComment());
            when(taskCommentMapper.toResponse(any(TaskComment.class)))
                    .thenReturn(getTaskCommentResponse(uuid));

            mockMvc.perform(put("/comments/{uuid}", uuid)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(getTaskCommentRequest(uuid))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.uuid").value(uuid.toString()));
        }

        @Test
        void updateTaskComment_notFound_returns404() throws Exception {
            UUID uuid = UUID.randomUUID();

            when(taskCommentService.updateTaskComment(any(UUID.class), any(TaskComment.class)))
                    .thenThrow(new EntityNotFoundException(TaskComment.class, uuid));
            when(taskCommentMapper.toEntity(any(TaskCommentRequest.Detail.class)))
                    .thenReturn(new TaskComment());

            mockMvc.perform(put("/comments/{uuid}", uuid)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(getTaskCommentRequest(uuid))))
                    .andExpect(status().isNotFound());
        }

        @Test
        void updateTaskComment_invalid_returnsBadRequest() throws Exception {
            mockMvc.perform(put("/comments/{uuid}", UUID.randomUUID())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(TaskCommentRequest.Detail.builder()
                                    .comment("a".repeat(2048))
                                    .build())))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("DELETE /comments")
    class DeleteTaskComment {
        @Test
        void deleteTaskComment_Found_returns204() throws Exception {
            doNothing().when(taskCommentService).deleteTaskComment(any(UUID.class));

            mockMvc.perform(delete("/comments/{uuid}", UUID.randomUUID()))
                    .andExpect(status().isNoContent());
        }

        @Test
        void deleteTaskComment_notFound_returns404() throws Exception {
            doThrow(new EntityNotFoundException(TaskComment.class, UUID.randomUUID()))
                    .when(taskCommentService).deleteTaskComment(any(UUID.class));

            mockMvc.perform(delete("/comments/{uuid}", UUID.randomUUID()))
                    .andExpect(status().isNotFound());
        }
    }
}
