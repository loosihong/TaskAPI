package com.example.TaskAPI.task.mapper;

import com.example.TaskAPI.core.BaseMapperTest;
import com.example.TaskAPI.task.api.dto.TaskDetailRequest;
import com.example.TaskAPI.task.api.dto.TaskDetailResponse;
import com.example.TaskAPI.task.api.dto.TaskRequest;
import com.example.TaskAPI.task.api.dto.TaskResponse;
import com.example.TaskAPI.task.domain.entity.Task;
import com.example.TaskAPI.task.domain.entity.TaskDetail;
import com.example.TaskAPI.task.domain.enums.Priority;
import com.example.TaskAPI.task.domain.enums.TaskStatus;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.util.UUID;

public class TaskMapperTest extends BaseMapperTest {
    private static final String[] IGNORE_TASK_FIELDS = {
            Task.Fields.taskDetail,
            Task.Fields.taskComments
    };

    private final TaskMapper taskMapper = Mappers.getMapper(TaskMapper.class);

    @Test
    void toEntity_mapsTaskRequest() {
        TaskRequest.Detail taskRequest = TaskRequest.Detail.builder()
                .uuid(UUID.randomUUID())
                .title("Buy groceries")
                .status(TaskStatus.TODO)
                .version(1)
                .build();
        Task expectedTask = Task.builder()
                .uuid(taskRequest.uuid())
                .title(taskRequest.title())
                .status(taskRequest.status())
                .version(taskRequest.version())
                .build();
        Task actualTask = taskMapper.toEntity(taskRequest);

        assertThatMappedEntity(actualTask, IGNORE_TASK_FIELDS)
                .isEqualTo(expectedTask);
    }

    @Test
    void toEntity_mapsTaskDetailRequest() {
        TaskDetailRequest.Detail taskDetailRequest = TaskDetailRequest.Detail.builder()
                .dueDate(LocalDate.now().plusDays(1))
                .description("For dinner")
                .priority(Priority.MEDIUM)
                .version(1)
                .build();
        TaskDetail expectedTaskDetail = TaskDetail.builder()
                .dueDate(taskDetailRequest.dueDate())
                .description(taskDetailRequest.description())
                .priority(taskDetailRequest.priority())
                .version(taskDetailRequest.version())
                .build();
        TaskDetail actualTaskDetail = taskMapper.toEntity(taskDetailRequest);

        assertThatMappedEntity(actualTaskDetail)
                .isEqualTo(expectedTaskDetail);
    }

    @Test
    void toResponse_mapsTask() {
        Task task = Task.builder()
                .uuid(UUID.randomUUID())
                .title("Buy groceries")
                .status(TaskStatus.TODO)
                .version(1)
                .build();
        TaskResponse.Detail expectedTaskResponse = TaskResponse.Detail.builder()
                .uuid(task.getUuid())
                .title(task.getTitle())
                .status(task.getStatus())
                .version(task.getVersion())
                .build();
        TaskResponse.Detail actualTaskResponse = taskMapper.toResponse(task);

        assertThatMappedEntity(actualTaskResponse)
                .isEqualTo(expectedTaskResponse);
    }

    @Test
    void toDetailResponse_mapsTask() {
        TaskDetail taskDetail = TaskDetail.builder()
                .dueDate(LocalDate.now().plusDays(1))
                .description("For lunch")
                .priority(Priority.LOW)
                .version(1)
                .build();
        TaskDetailResponse.Detail expectedDetailResponse = TaskDetailResponse.Detail.builder()
                .dueDate(taskDetail.getDueDate())
                .description(taskDetail.getDescription())
                .priority(taskDetail.getPriority())
                .version(taskDetail.getVersion())
                .build();
        TaskDetailResponse.Detail actualDetailResponse = taskMapper.toTaskDetailResponse(taskDetail);

        assertThatMappedEntity(actualDetailResponse)
                .isEqualTo(expectedDetailResponse);
    }

    @Test
    void update_task_updatesOnlyMappedFields() {
        Task source = Task.builder()
                .id(1L)
                .uuid(UUID.randomUUID())
                .title("Buy groceries")
                .status(TaskStatus.TODO)
                .version(1)
                .build();
        Task target = new Task();

        taskMapper.update(source, target);

        assertThatMappedEntity(target, IGNORE_TASK_FIELDS)
                .isEqualTo(source);
    }
}
