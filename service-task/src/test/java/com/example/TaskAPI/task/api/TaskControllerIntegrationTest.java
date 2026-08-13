package com.example.TaskAPI.task.api;

import com.example.TaskAPI.core.BaseWebIntegrationTest;
import com.example.TaskAPI.core.audit.AuditLogRepository;
import com.example.TaskAPI.task.api.dto.TaskAssigneeRequest;
import com.example.TaskAPI.task.api.dto.TaskDashboardSearchRequest;
import com.example.TaskAPI.task.api.dto.TaskDetailRequest;
import com.example.TaskAPI.task.api.dto.TaskListSearchRequest;
import com.example.TaskAPI.task.api.dto.TaskRequest;
import com.example.TaskAPI.task.domain.enums.Priority;
import com.example.TaskAPI.task.domain.enums.TaskStatus;
import com.example.TaskAPI.task.domain.query.TaskDashboardFilter;
import com.example.TaskAPI.task.domain.query.TaskListFilter;
import com.example.TaskAPI.task.scheduler.SendTaskReminder;
import com.example.TaskAPI.user.domain.entity.User;
import org.jobrunr.scheduling.JobRequestScheduler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

public class TaskControllerIntegrationTest extends BaseWebIntegrationTest {
    @Autowired
    private AuditLogRepository auditLogRepository;
    @MockitoBean
    private JobRequestScheduler jobRequestScheduler;

    @Test
    void getAllTasks_returnsPersistedTasks() throws Exception {
        createTask(getTaskRequest());
        createTask(getTaskRequest());

        mockMvc.perform(get("/tasks")
                        .with(authenticated()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getTaskByUuid_found_returnsTask() throws Exception {
        UUID uuid = createTask(getTaskRequest());

        mockMvc.perform(get("/tasks/{uuid}", uuid)
                        .with(authenticated()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(uuid.toString()));
    }

    @Test
    void createTask_noDetail_returnsTask() throws Exception {
        TaskRequest.Detail taskRequest = TaskRequest.Detail.builder()
                .title("Buy groceries")
                .status(TaskStatus.TODO)
                .build();

        mockMvc.perform(post("/tasks")
                        .with(authenticated())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uuid").exists())
                .andExpect(jsonPath("$.taskDetail").doesNotExist());
    }

    @Test
    void createTask_withDetail_returnsBoth() throws Exception {
        mockMvc.perform(post("/tasks")
                        .with(authenticated())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getTaskRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uuid").exists())
                .andExpect(jsonPath("$.taskDetail").exists());
    }

    @Test
    void updateTask_preservesDetail() throws Exception {
        UUID uuid = createTask(getTaskRequest());
        TaskRequest.Detail taskRequest = TaskRequest.Detail.builder()
                .uuid(uuid)
                .title("Earn money")
                .status(TaskStatus.IN_PROGRESS)
                .build();

        mockMvc.perform(put("/tasks/{uuid}", uuid)
                        .with(authenticated())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(uuid.toString()))
                .andExpect(jsonPath("$.taskDetail").exists());
    }

    @Test
    void deleteTask_softDeletes_returnsNotFound() throws Exception {
        UUID uuid = createTask(getTaskRequest());

        mockMvc.perform(delete("/tasks/{uuid}", uuid)
                        .with(authenticated()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/tasks/{uuid}", uuid)
                        .with(authenticated()))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateTaskDetail_returnsTaskDetail() throws Exception {
        UUID uuid = createTask(getTaskRequest());
        TaskDetailRequest.Detail taskDetailRequest = TaskDetailRequest.Detail.builder()
                .description("North London forever")
                .priority(Priority.HIGH)
                .dueDate(LocalDate.now().plusDays(10))
                .build();

        mockMvc.perform(put("/tasks/{uuid}/detail", uuid)
                        .with(authenticated())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskDetailRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("North London forever"));
    }

    @Test
    void updateTask_persistsAuditLogAsync() throws Exception {
        UUID uuid = createTask(getTaskRequest());

        mockMvc.perform(put("/tasks/{uuid}", uuid)
                        .with(authenticated())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                TaskRequest.Detail.builder()
                                        .uuid(uuid)
                                        .title("updated")
                                        .status(TaskStatus.IN_PROGRESS)
                                        .build())))
                .andExpect(status().isOk());

        await().atMost(Duration.ofSeconds(2))
                .untilAsserted(() -> assertThat(auditLogRepository.findByEntityUuid(uuid)).isNotEmpty());
    }

    @Test
    void anyEndpoint_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/tasks"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateTaskAssignees_returnsTask() throws Exception {
        UUID taskUuid = createTask(getTaskRequest());
        User user1 = createUser("user1");
        User user2 = createUser("user2");
        TaskAssigneeRequest.Assign assigneeRequest = TaskAssigneeRequest.Assign.builder()
                .assigneeUuids(Set.of(user1.getUuid(), user2.getUuid()))
                .build();

        mockMvc.perform(put("/tasks/{taskUuid}/assignees", taskUuid)
                        .with(authenticated())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assigneeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(taskUuid.toString()))
                .andExpect(jsonPath("$.taskAssignees.length()").value(2));
    }

    @Test
    void searchTaskList_allFilters_appliesAllConditions() throws Exception {
        User user1 = createUser("user1");
        TaskRequest.Detail taskRequest1 = TaskRequest.Detail.builder()
                .title("Read book")
                .status(TaskStatus.DONE)
                .assigneeUuids(Set.of(user1.getUuid()))
                .build();
        TaskRequest.Detail taskRequest2 = TaskRequest.Detail.builder()
                .title("Arsenal")
                .status(TaskStatus.IN_PROGRESS)
                .build();
        UUID task1Uuid = createTask(taskRequest1);

        createTask(getTaskRequest());
        createTask(taskRequest2);

        mockMvc.perform(post("/tasks/listing")
                        .with(authenticated())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TaskListSearchRequest(
                                TaskListFilter.builder()
                                        .title(taskRequest1.title())
                                        .statuses(List.of(taskRequest1.status()))
                                        .createdAtFrom(LocalDateTime.now().minusHours(1))
                                        .createdAtTo(LocalDateTime.now().plusHours(1))
                                        .build(),
                                null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.content[0].uuid").value(task1Uuid.toString()))
                .andExpect(jsonPath("$.content[0].title").value(taskRequest1.title()))
                .andExpect(jsonPath("$.content[0].status").value(taskRequest1.status().getCode()));
    }

    @Test
    void searchTaskDashboard_allFilters_appliesAllConditions() throws Exception {
        TaskRequest.Detail taskRequest1 = TaskRequest.Detail.builder()
                .title("Read book")
                .status(TaskStatus.DONE)
                .assigneeUuids(Set.of(loginUser.getUuid()))
                .taskDetail(getTaskDetailRequest())
                .build();
        TaskRequest.Detail taskRequest2 = TaskRequest.Detail.builder()
                .title("Arsenal")
                .status(TaskStatus.IN_PROGRESS)
                .build();
        UUID task1Uuid = createTask(taskRequest1);

        createTask(getTaskRequest());
        createTask(taskRequest2);

        mockMvc.perform(post("/tasks/dashboard")
                        .with(authenticated())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TaskDashboardSearchRequest(
                                TaskDashboardFilter.builder()
                                        .title(taskRequest1.title())
                                        .statuses(List.of(taskRequest1.status()))
                                        .priorities(List.of(taskRequest1.taskDetail().priority()))
                                        .updatedAtFrom(LocalDateTime.now().minusHours(1))
                                        .updatedAtTo(LocalDateTime.now().plusHours(1))
                                        .updatedByUuids(List.of(loginUser.getUuid()))
                                        .build(),
                                null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.content[0].taskUuid").value(task1Uuid.toString()))
                .andExpect(jsonPath("$.content[0].title").value(taskRequest1.title()))
                .andExpect(jsonPath("$.content[0].status").value(taskRequest1.status().getCode()))
                .andExpect(jsonPath("$.content[0].priority")
                        .value(taskRequest1.taskDetail().priority().toString()))
                .andExpect(jsonPath("$.content[0].updatedByName").value(loginUser.getUsername()));
    }

    @Test
    void createTask_withDueDate_schedulesReminderJob() throws Exception {
        createTask(getTaskRequest());

        await().atMost(Duration.ofSeconds(3)).untilAsserted(() ->
                verify(jobRequestScheduler).schedule(
                        any(UUID.class),
                        any(Instant.class),
                        any(SendTaskReminder.class)));
    }

    @Test
    void deleteTask_cancelsReminderJob() throws Exception {
        UUID taskUuid = createTask(getTaskRequest());

        mockMvc.perform(delete("/tasks/{taskUuid}", taskUuid)
                        .with(authenticated()))
                .andExpect(status().isNoContent());

        await().atMost(Duration.ofSeconds(3)).untilAsserted(() ->
                verify(jobRequestScheduler, atLeastOnce()).delete(any(UUID.class), anyString()));
    }

    private TaskRequest.Detail getTaskRequest() {
        return TaskRequest.Detail.builder()
                .title("Buy groceries")
                .status(TaskStatus.TODO)
                .taskDetail(getTaskDetailRequest())
                .build();
    }

    private TaskDetailRequest.Detail getTaskDetailRequest() {
        return TaskDetailRequest.Detail.builder()
                .description("No more food")
                .priority(Priority.MEDIUM)
                .dueDate(LocalDate.now().plusDays(1))
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
}
