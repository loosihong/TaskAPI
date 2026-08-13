package com.example.TaskAPI.task.api;

import com.example.TaskAPI.core.BaseWebIntegrationTest;
import com.example.TaskAPI.task.api.dto.TaskCommentRequest;
import com.example.TaskAPI.task.api.dto.TaskRequest;
import com.example.TaskAPI.task.domain.enums.TaskStatus;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TaskCommentControllerIntegrationTest extends BaseWebIntegrationTest {
    @Test
    void getTaskCommentListItemsByTaskUuid_returnsPersistedTaskComments() throws Exception {
        UUID taskUuid = createTask(validTaskRequest());
        createTaskComment(taskUuid, getTaskCommentRequest());
        createTaskComment(taskUuid, getTaskCommentRequest());

        mockMvc.perform(get("/tasks/{taskUuid}/comments", taskUuid)
                        .with(authenticated()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void createTaskComment_returnsTaskComment() throws Exception {
        UUID taskUuid = createTask(validTaskRequest());

        mockMvc.perform(post("/tasks/{taskUuid}/comments", taskUuid)
                        .with(authenticated())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getTaskCommentRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uuid").exists());
    }

    @Test
    void updateTaskComment_returnsTaskComment() throws Exception {
        UUID taskUuid = createTask(validTaskRequest());
        UUID uuid = createTaskComment(taskUuid, validTaskCommentRequest());
        TaskCommentRequest.Detail taskCommentRequest = TaskCommentRequest.Detail.builder()
                .uuid(uuid)
                .comment("North London forever")
                .build();

        mockMvc.perform(put("/comments/{uuid}", uuid)
                        .with(authenticated())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskCommentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(uuid.toString()));
    }

    @Test
    void deleteTaskComment_softDeletes_returnsNotFound() throws Exception {
        UUID taskUuid = createTask(validTaskRequest());
        UUID uuid = createTaskComment(taskUuid, validTaskCommentRequest());

        mockMvc.perform(delete("/comments/{uuid}", uuid)
                        .with(authenticated()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/tasks/{taskUuid}/comments", taskUuid)
                        .with(authenticated()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void anyEndpoint_unauthenticated_returns401() throws Exception {
        mockMvc.perform(put("/comments/{uuid}", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    private TaskRequest.Detail validTaskRequest() {
        return TaskRequest.Detail.builder()
                .title("Buy groceries")
                .status(TaskStatus.TODO)
                .build();
    }

    private TaskCommentRequest.Detail validTaskCommentRequest() {
        return TaskCommentRequest.Detail.builder()
                .comment("Arsenal")
                .build();
    }

    private TaskCommentRequest.Detail getTaskCommentRequest() {
        return TaskCommentRequest.Detail.builder()
                .comment("Arsenal")
                .build();
    }

    private UUID createTask(TaskRequest.Detail taskRequest) throws Exception {
        return UUID.fromString(objectMapper.readTree(
                        mockMvc.perform(post("/tasks")
                                        .with(authenticated())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(taskRequest)))
                                .andReturn().getResponse().getContentAsString())
                .get("uuid").asString());
    }

    private UUID createTaskComment(UUID taskUuid, TaskCommentRequest.Detail taskCommentRequest) throws Exception {
        return UUID.fromString(objectMapper.readTree(
                        mockMvc.perform(post("/tasks/{taskUuid}/comments", taskUuid)
                                        .with(authenticated())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(taskCommentRequest)))
                                .andReturn().getResponse().getContentAsString())
                .get("uuid").asString());
    }
}
