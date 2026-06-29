package com.example.TaskAPI.task.service;

import com.example.TaskAPI.core.exception.DataValidationException;
import com.example.TaskAPI.core.exception.EntityNotFoundException;
import com.example.TaskAPI.task.domain.entity.Task;
import com.example.TaskAPI.task.domain.entity.TaskAssignee;
import com.example.TaskAPI.task.domain.entity.TaskDetail;
import com.example.TaskAPI.task.domain.repository.TaskRepository;
import com.example.TaskAPI.task.mapper.TaskMapper;
import com.example.TaskAPI.user.domain.entity.User;
import com.example.TaskAPI.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {
    @Mock
    private TaskMapper taskMapper;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private TaskService taskService;

    private Task buildFullTask(UUID uuid) {
        Set<TaskAssignee> taskAssignees = new HashSet<>(Set.of(
                TaskAssignee.builder()
                        .user(User.builder().uuid(UUID.randomUUID()).build())
                        .build(),
                TaskAssignee.builder()
                        .user(User.builder().uuid(UUID.randomUUID()).build())
                        .build()));

        return Task.builder()
                .uuid(uuid)
                .taskDetail(TaskDetail.builder().build())
                .taskAssignees(taskAssignees)
                .build();
    }

    @Nested
    @DisplayName("Read Operations")
    class ReadOperations {
        private UUID uuid;

        @BeforeEach
        void setUp() {
            uuid = UUID.randomUUID();
        }

        @Test
        void getAllTasks_returnsList() {
            when(taskRepository.findAll())
                    .thenReturn(List.of(buildFullTask(UUID.randomUUID()), buildFullTask(UUID.randomUUID())));

            assertThat(taskService.getAllTasks()).hasSize(2);
            verify(taskRepository).findAll();
        }

        @Test
        void getTaskByUuid_found_returnsTask() {
            Task task = Task.builder().build();

            when(taskRepository.findByUuid(any(UUID.class)))
                    .thenReturn(Optional.of(task));

            Optional<Task> result = taskService.getTaskByUuid(uuid);

            assertThat(result).isPresent().contains(task);
            verify(taskRepository).findByUuid(uuid);
        }

        @Test
        void getTaskWithDetailByUuid_found_returnsTaskWithDetail() {
            Task task = buildFullTask(uuid);

            when(taskRepository.findWithDetailByUuid(any(UUID.class)))
                    .thenReturn(Optional.of(task));

            Optional<Task> result = taskService.getWithDetailByUuid(uuid);

            assertThat(result).isPresent();
            assertThat(result.get().getTaskDetail()).isEqualTo(task.getTaskDetail());
            verify(taskRepository).findWithDetailByUuid(uuid);
        }
    }

    @Nested
    @DisplayName("Create Operations")
    class CreateOperations {
        private UUID uuid;

        @BeforeEach
        void setUp() {
            uuid = UUID.randomUUID();
        }

        @Test
        void createTask_noJoins_savesTask() {
            Task task = Task.builder()
                    .uuid(uuid)
                    .build();

            when(taskRepository.save(any(Task.class)))
                    .thenReturn(task);

            Task result = taskService.createTask(task, null, null);

            assertThat(result).isEqualTo(task);
            assertThat(result.getTaskDetail()).isNull();
            verify(taskRepository).save(task);
        }

        @Test
        void createTask_withAllJoins_savesTaskAndAllJoins() {
            Task task = buildFullTask(uuid);

            when(userRepository.findAllByUuidIn(anyCollection()))
                    .thenReturn(task.getTaskAssignees().stream()
                            .map(TaskAssignee::getUser)
                            .toList());
            when(taskRepository.save(any(Task.class)))
                    .thenReturn(task);

            Task result = taskService.createTask(task, task.getTaskDetail(),
                    Set.of(UUID.randomUUID(), UUID.randomUUID()));

            assertThat(result.getTaskDetail()).isEqualTo(task.getTaskDetail());
            assertThat(result.getTaskDetail().getTask()).isEqualTo(task);
            assertThat(result).isEqualTo(task);
            verify(taskRepository).save(task);
        }

        @Test
        void createTask_withNull_throwsException() {
            assertThatThrownBy(() -> taskService.createTask(null, null, null))
                    .isInstanceOf(DataValidationException.class);
        }
    }

    @Nested
    @DisplayName("Update Operations")
    class UpdateOperations {
        private UUID uuid;

        @BeforeEach
        void setUp() {
            uuid = UUID.randomUUID();
        }

        @Test
        void updateTask_noJoins_returnsTask() {
            Task foundTask = Task.builder()
                    .uuid(uuid)
                    .build();
            Task savedTask = Task.builder()
                    .uuid(uuid)
                    .title("Buy groceries")
                    .build();

            when(taskRepository.findWithDetailByUuid(any(UUID.class)))
                    .thenReturn(Optional.of(foundTask));
            when(taskRepository.save(any(Task.class)))
                    .thenReturn(savedTask);

            Task result = taskService.updateTask(uuid, savedTask, null);

            assertThat(result).isEqualTo(savedTask);
            verify(taskMapper).update(savedTask, foundTask);
            verify(taskRepository).save(foundTask);
        }

        @Test
        void updateTask_notFound_throwsException() {
            when(taskRepository.findWithDetailByUuid(any(UUID.class)))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.updateTask(uuid, buildFullTask(uuid), null))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        void updateTask_withAllJoins_returnsTaskAndAllJoins() {
            Task task = buildFullTask(uuid);

            List<TaskAssignee> taskAssignees = task.getTaskAssignees().stream().toList();
            UUID newUserUuid = UUID.randomUUID();

            when(taskRepository.findWithDetailByUuid(any(UUID.class)))
                    .thenReturn(Optional.of(task));
            when(userRepository.findAllByUuidIn(anyCollection()))
                    .thenReturn(List.of(
                            taskAssignees.getFirst().getUser(),
                            User.builder().uuid(newUserUuid).build()));
            when(taskRepository.save(any(Task.class)))
                    .thenReturn(task);

            Task result = taskService.updateTask(uuid, task, Set.of(
                    taskAssignees.getFirst().getUser().getUuid(),
                    newUserUuid));

            assertThat(result).isNotNull();
            assertThat(result.getTaskAssignees()).anyMatch(
                    taskAssignee -> taskAssignee.getUser().getUuid().equals(newUserUuid));
            verify(taskRepository).save(task);
        }

        @Test
        void updateTaskDetail_found_returnsTaskDetail() {
            TaskDetail foundTaskDetail = TaskDetail.builder().build();
            TaskDetail savedTaskDetail = TaskDetail.builder()
                    .description("North London")
                    .build();
            Task task = Task.builder()
                    .uuid(uuid)
                    .taskDetail(foundTaskDetail)
                    .build();

            when(taskRepository.findWithDetailByUuid(any(UUID.class)))
                    .thenReturn(Optional.of(task));

            TaskDetail result = taskService.updateTaskDetail(uuid, savedTaskDetail);

            assertThat(result).isSameAs(foundTaskDetail);
            verify(taskMapper).update(savedTaskDetail, foundTaskDetail);
        }

        @Test
        void updateTaskDetail_taskNotFound_throwsException() {
            when(taskRepository.findWithDetailByUuid(any(UUID.class)))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.updateTaskDetail(uuid, TaskDetail.builder().build()))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        void updateTaskDetail_detailNotFound_throwsException() {
            when(taskRepository.findWithDetailByUuid(any(UUID.class)))
                    .thenReturn(Optional.of(Task.builder()
                            .uuid(uuid)
                            .build()));

            assertThatThrownBy(() -> taskService.updateTaskDetail(uuid, TaskDetail.builder().build()))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        void updateTaskAssignees_userFound_returnsTask() {
            Task task = buildFullTask(uuid);

            List<TaskAssignee> taskAssignees = task.getTaskAssignees().stream().toList();
            UUID newUserUuid = UUID.randomUUID();

            when(taskRepository.findWithAssigneesByUuid(any(UUID.class)))
                    .thenReturn(Optional.of(task));
            when(userRepository.findAllByUuidIn(anyCollection()))
                    .thenReturn(List.of(
                            taskAssignees.getFirst().getUser(),
                            User.builder().uuid(newUserUuid).build()));
            when(taskRepository.save(any(Task.class)))
                    .thenReturn(task);

            Task result = taskService.updateTaskAssignees(uuid, Set.of(
                    taskAssignees.getFirst().getUser().getUuid(),
                    newUserUuid));

            assertThat(result).isNotNull();
            assertThat(result.getTaskAssignees()).anyMatch(
                    taskAssignee -> taskAssignee.getUser().getUuid().equals(newUserUuid));
            verify(taskRepository).save(task);
        }

        @Test
        void updateTaskAssignees_userNotFound_throwsException() {
            Task task = buildFullTask(uuid);

            when(taskRepository.findWithAssigneesByUuid(any(UUID.class)))
                    .thenReturn(Optional.of(task));
            when(userRepository.findAllByUuidIn(anyCollection()))
                    .thenReturn(List.of(User.builder().uuid(UUID.randomUUID()).build()));

            assertThatThrownBy(() -> taskService.updateTaskAssignees(uuid,
                    Set.of(UUID.randomUUID(), UUID.randomUUID())))
                    .isInstanceOf(DataValidationException.class);
        }
    }

    @Nested
    @DisplayName("Delete Operations")
    class DeleteOperations {
        @Test
        void deleteTask_found_deletesTask() {
            UUID uuid = UUID.randomUUID();

            when(taskRepository.existsByUuid(any(UUID.class)))
                    .thenReturn(true);

            taskService.deleteTask(uuid);

            verify(taskRepository).deleteByUuid(uuid);
        }

        @Test
        void deleteTask_notFound_throwsException() {
            when(taskRepository.existsByUuid(any(UUID.class)))
                    .thenReturn(false);

            assertThatThrownBy(() -> taskService.deleteTask(UUID.randomUUID()))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }
}

