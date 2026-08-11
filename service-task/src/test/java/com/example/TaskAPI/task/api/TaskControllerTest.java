package com.example.TaskAPI.task.api;

import com.example.TaskAPI.core.BaseControllerTest;
import com.example.TaskAPI.core.exception.DataValidationException;
import com.example.TaskAPI.core.exception.EntityNotFoundException;
import com.example.TaskAPI.task.api.dto.TaskAssigneeRequest;
import com.example.TaskAPI.task.api.dto.TaskDashboardSearchRequest;
import com.example.TaskAPI.task.api.dto.TaskDetailRequest;
import com.example.TaskAPI.task.api.dto.TaskDetailResponse;
import com.example.TaskAPI.task.api.dto.TaskListSearchRequest;
import com.example.TaskAPI.task.api.dto.TaskRequest;
import com.example.TaskAPI.task.api.dto.TaskResponse;
import com.example.TaskAPI.task.domain.entity.Task;
import com.example.TaskAPI.task.domain.entity.TaskDetail;
import com.example.TaskAPI.task.domain.enums.Priority;
import com.example.TaskAPI.task.domain.enums.TaskStatus;
import com.example.TaskAPI.task.domain.query.TaskDashboardFilter;
import com.example.TaskAPI.task.domain.query.TaskDashboardItem;
import com.example.TaskAPI.task.domain.query.TaskListFilter;
import com.example.TaskAPI.task.mapper.TaskCommentMapper;
import com.example.TaskAPI.task.mapper.TaskMapper;
import com.example.TaskAPI.task.service.TaskCommentService;
import com.example.TaskAPI.task.service.TaskService;
import com.example.TaskAPI.user.api.dto.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anySet;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
public class TaskControllerTest extends BaseControllerTest {
    @MockitoBean
    private TaskService taskService;

    @MockitoBean
    private TaskMapper taskMapper;

    @MockitoBean
    private TaskCommentService taskCommentService;

    @MockitoBean
    private TaskCommentMapper taskCommentMapper;

    private TaskRequest.Detail getTaskRequest(UUID uuid) {
        return TaskRequest.Detail.builder()
                .uuid(uuid)
                .title("Buy groceries")
                .status(TaskStatus.TODO)
                .build();
    }

    private TaskRequest.Detail getFullTaskRequest(UUID uuid) {
        return TaskRequest.Detail.builder()
                .uuid(uuid)
                .title("Buy groceries")
                .status(TaskStatus.TODO)
                .assigneeUuids(new HashSet<>(List.of(UUID.randomUUID(), UUID.randomUUID())))
                .taskDetail(TaskDetailRequest.Detail.builder()
                        .priority(Priority.LOW)
                        .build())
                .build();
    }

    private TaskResponse.Detail getTaskResponse(UUID uuid) {
        return TaskResponse.Detail.builder()
                .uuid(uuid)
                .title("Buy groceries")
                .status(TaskStatus.TODO)
                .build();
    }

    private TaskResponse.Detail getFullTaskResponse(UUID uuid) {
        return TaskResponse.Detail.builder()
                .uuid(uuid)
                .title("Buy groceries")
                .status(TaskStatus.TODO)
                .taskAssignees(new HashSet<>(List.of(
                        UserResponse.Summary.builder()
                                .uuid(UUID.randomUUID())
                                .username("user1")
                                .build(),
                        UserResponse.Summary.builder()
                                .uuid(UUID.randomUUID())
                                .username("user2")
                                .build()
                )))
                .taskDetail(TaskDetailResponse.Detail.builder()
                        .priority(Priority.LOW)
                        .build())
                .build();
    }

    @Nested
    @DisplayName("GET /tasks")
    class GetTask {
        @Test
        void getAllTasks_returnsList() throws Exception {
            when(taskService.getAllTasks())
                    .thenReturn(List.of(new Task(), new Task()));
            when(taskMapper.toResponseList(anyList()))
                    .thenReturn(List.of(
                            getFullTaskResponse(UUID.randomUUID()),
                            getFullTaskResponse(UUID.randomUUID())));

            mockMvc.perform(get("/tasks"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));
        }

        @Test
        void getTaskByUuid_found_returnsTask() throws Exception {
            UUID uuid = UUID.randomUUID();

            when(taskService.getWithDetailByUuid(any(UUID.class)))
                    .thenReturn(Optional.of(new Task()));
            when(taskMapper.toResponse(any(Task.class)))
                    .thenReturn(getFullTaskResponse(uuid));

            mockMvc.perform(get("/tasks/{uuid}", uuid))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.uuid").value(uuid.toString()))
                    .andExpect(jsonPath("$.taskDetail").exists())
                    .andExpect(jsonPath("$.taskAssignees").isNotEmpty());
        }

        @Test
        void getTaskByUuid_notFound_returns404() throws Exception {
            when(taskService.getWithDetailByUuid(any(UUID.class)))
                    .thenReturn(Optional.empty());

            mockMvc.perform(get("/tasks/{uuid}", UUID.randomUUID()))
                    .andExpect((status().isNotFound()));
        }
    }

    @Nested
    @DisplayName("POST /tasks/listing")
    class SearchTaskList {
        @Test
        void searchTaskList_defaultFilter_returnsPagedResponse() throws Exception {
            when(taskService.searchTaskList(any(TaskListFilter.class), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(new Task())));
            when(taskMapper.toResponse(any(Task.class)))
                    .thenReturn(getFullTaskResponse(UUID.randomUUID()));

            mockMvc.perform(post("/tasks/listing")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new TaskListSearchRequest(
                                    TaskListFilter.builder().build(),
                                    null))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.totalPages").value(1));
        }

        @Test
        void searchTaskList_invalidFilter_returnsException() throws Exception {
            when(taskService.searchTaskList(any(TaskListFilter.class), any(Pageable.class)))
                    .thenThrow(new DataValidationException(TaskListFilter.class, ""));

            mockMvc.perform(post("/tasks/listing")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new TaskListSearchRequest(
                                    TaskListFilter.builder()
                                            .createdAtFrom(LocalDateTime.now())
                                            .createdAtTo(LocalDateTime.now().minusDays(1))
                                            .build(),
                                    null))))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /tasks/dashboard")
    class SearchTaskDashboard {
        @Test
        void searchTaskDashboard_defaultFilter_returnsPagedResponse() throws Exception {
            when(taskService.searchTaskDashboard(any(TaskDashboardFilter.class), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(new TaskDashboardItem(
                            UUID.randomUUID(),
                            "",
                            null,
                            LocalDateTime.now(),
                            "",
                            LocalDateTime.now(),
                            "", Priority.LOW,
                            ""))));

            mockMvc.perform(post("/tasks/dashboard")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new TaskDashboardSearchRequest(
                                    TaskDashboardFilter.builder().build(),
                                    null))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.totalPages").value(1));
        }

        @Test
        void searchTaskDashboard_invalidFilter_returnsException() throws Exception {
            when(taskService.searchTaskDashboard(any(TaskDashboardFilter.class), any(Pageable.class)))
                    .thenThrow(new DataValidationException(TaskDashboardFilter.class, ""));

            mockMvc.perform(post("/tasks/dashboard")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new TaskDashboardSearchRequest(
                                    TaskDashboardFilter.builder().build(),
                                    null))))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /tasks")
    class CreateTask {
        @Test
        void createTask_noJoins_returnsTask() throws Exception {
            UUID uuid = UUID.randomUUID();

            when(taskService.createTask(any(Task.class), isNull(), anySet()))
                    .thenReturn(new Task());
            when(taskMapper.toEntity(any(TaskRequest.Detail.class)))
                    .thenReturn(new Task());
            when(taskMapper.toResponse(any(Task.class)))
                    .thenReturn(getTaskResponse(uuid));

            mockMvc.perform(post("/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(getTaskRequest(uuid))))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.uuid").value(uuid.toString()))
                    .andExpect(jsonPath("$.taskDetail").doesNotExist())
                    .andExpect(jsonPath("$.taskAssignees").isEmpty());
        }

        @Test
        void createTask_allJoins_returnsTask() throws Exception {
            UUID uuid = UUID.randomUUID();

            when(taskService.createTask(any(Task.class), any(TaskDetail.class), anySet()))
                    .thenReturn(new Task());
            when(taskMapper.toEntity(any(TaskRequest.Detail.class)))
                    .thenReturn(new Task());
            when(taskMapper.toEntity(any(TaskDetailRequest.Detail.class)))
                    .thenReturn(new TaskDetail());
            when(taskMapper.toResponse(any(Task.class)))
                    .thenReturn(getFullTaskResponse(uuid));

            mockMvc.perform(post("/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(getFullTaskRequest(uuid))))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.uuid").value(uuid.toString()))
                    .andExpect(jsonPath("$.taskDetail").exists())
                    .andExpect(jsonPath("$.taskAssignees.length()").value(2));
        }

        @Test
        void createTask_invalid_returnsBadRequest() throws Exception {
            mockMvc.perform(post("/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    TaskRequest.Detail.builder().build())))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PUT /tasks")
    class UpdateTask {
        @Test
        void updateTask_noJoins_returnsTask() throws Exception {
            UUID uuid = UUID.randomUUID();

            when(taskService.updateTask(any(UUID.class), any(Task.class), anySet()))
                    .thenReturn(new Task());
            when(taskMapper.toEntity(any(TaskRequest.Detail.class)))
                    .thenReturn(new Task());
            when(taskMapper.toResponse(any(Task.class)))
                    .thenReturn(getTaskResponse(uuid));

            mockMvc.perform(put("/tasks/{uuid}", uuid)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(getTaskRequest(uuid))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.uuid").value(uuid.toString()))
                    .andExpect(jsonPath("$.taskDetail").doesNotExist())
                    .andExpect(jsonPath("$.taskAssignees").isEmpty());
        }

        @Test
        void updateTask_allJoins_returnsTask() throws Exception {
            UUID uuid = UUID.randomUUID();

            when(taskService.updateTask(any(UUID.class), any(Task.class), anySet()))
                    .thenReturn(new Task());
            when(taskMapper.toEntity(any(TaskRequest.Detail.class)))
                    .thenReturn(new Task());
            when(taskMapper.toResponse(any(Task.class)))
                    .thenReturn(getFullTaskResponse(uuid));

            mockMvc.perform(put("/tasks/{uuid}", uuid)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(getFullTaskRequest(uuid))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.uuid").value(uuid.toString()))
                    .andExpect(jsonPath("$.taskDetail").exists())
                    .andExpect(jsonPath("$.taskAssignees.length()").value(2));
        }

        @Test
        void updateTask_notFound_returns404() throws Exception {
            UUID uuid = UUID.randomUUID();

            when(taskService.updateTask(any(UUID.class), any(Task.class), anySet()))
                    .thenThrow(new EntityNotFoundException(Task.class, uuid));
            when(taskMapper.toEntity(any(TaskRequest.Detail.class)))
                    .thenReturn(new Task());

            mockMvc.perform(put("/tasks/{uuid}", uuid)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(getTaskRequest(uuid))))
                    .andExpect(status().isNotFound());
        }

        @Test
        void updateTask_invalid_returnsBadRequest() throws Exception {
            mockMvc.perform(put("/tasks/{uuid}", UUID.randomUUID())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    TaskRequest.Detail.builder().build())))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void updateTaskAssignees_foundTask_returnsTask() throws Exception {
            UUID uuid = UUID.randomUUID();

            when(taskService.updateTaskAssignees(any(UUID.class), anySet()))
                    .thenReturn(new Task());
            when(taskMapper.toResponse(any(Task.class)))
                    .thenReturn(getFullTaskResponse(uuid));

            mockMvc.perform(put("/tasks/{uuid}/assignees", uuid)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    TaskAssigneeRequest.Assign.builder()
                                            .assigneeUuids(Set.of(UUID.randomUUID(), UUID.randomUUID())))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.uuid").value(uuid.toString()))
                    .andExpect(jsonPath("$.taskAssignees.length()").value(2));
        }
    }

    @Nested
    @DisplayName("DELETE /tasks")
    class DeleteTask {
        private UUID uuid;

        @BeforeEach
        void setUp() {
            uuid = UUID.randomUUID();
        }

        @Test
        void deleteTask_found_returns204() throws Exception {
            doNothing().when(taskService).deleteTask(uuid);

            mockMvc.perform(delete("/tasks/{uuid}", uuid))
                    .andExpect(status().isNoContent());
        }

        @Test
        void deleteTask_notFound_returns404() throws Exception {
            doThrow(new EntityNotFoundException(Task.class, uuid))
                    .when(taskService).deleteTask(uuid);

            mockMvc.perform(delete("/tasks/{uuid}", uuid))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("/tasks/detail")
    class TaskDetailTest {
        private UUID taskUuid;

        @BeforeEach
        void setUp() {
            taskUuid = UUID.randomUUID();

            when(taskMapper.toEntity(any(TaskDetailRequest.Detail.class)))
                    .thenReturn(new TaskDetail());
            when(taskMapper.toTaskDetailResponse(any(TaskDetail.class)))
                    .thenReturn(TaskDetailResponse.Detail.builder()
                            .priority(Priority.MEDIUM)
                            .build());
        }

        @Test
        void taskDetail_found_returnsTask() throws Exception {
            when(taskService.updateTaskDetail(eq(taskUuid), any(TaskDetail.class)))
                    .thenReturn(TaskDetail.builder()
                            .priority(Priority.MEDIUM)
                            .build());

            mockMvc.perform(put("/tasks/{taskUuid}/detail", taskUuid)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(TaskDetail.builder()
                                    .priority(Priority.MEDIUM)
                                    .build())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.priority").value(Priority.MEDIUM.toString()));
        }

        @Test
        void taskDetail_notFound_returns404() throws Exception {
            when(taskService.updateTaskDetail(eq(taskUuid), any(TaskDetail.class)))
                    .thenThrow(new EntityNotFoundException(TaskDetail.class, taskUuid));

            mockMvc.perform(put("/tasks/{taskUuid}/detail", taskUuid)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(TaskDetail.builder()
                                    .priority(Priority.MEDIUM)
                                    .build())))
                    .andExpect(status().isNotFound());
        }

        @Test
        void taskDetail_invalid_returnsBadRequest() throws Exception {
            mockMvc.perform(put("/tasks/{taskUuid}/detail", UUID.randomUUID())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    TaskDetailRequest.Detail.builder()
                                            .description("a".repeat(2048))
                                            .build())))
                    .andExpect(status().isBadRequest());
        }
    }
}