package com.example.TaskAPI.task.service;

import com.example.TaskAPI.core.exception.DataValidationException;
import com.example.TaskAPI.core.exception.EntityNotFoundException;
import com.example.TaskAPI.task.api.dto.TaskCommentResponse;
import com.example.TaskAPI.task.domain.entity.Task;
import com.example.TaskAPI.task.domain.entity.TaskComment;
import com.example.TaskAPI.task.domain.repository.TaskCommentRepository;
import com.example.TaskAPI.task.domain.repository.TaskRepository;
import com.example.TaskAPI.task.mapper.TaskCommentMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TaskCommentServiceTest {
    @Mock
    private TaskCommentMapper taskCommentMapper;
    @Mock
    private TaskCommentRepository taskCommentRepository;
    @Mock
    private TaskRepository taskRepository;
    private TaskCommentService taskCommentService;

    private Task task;
    private TaskComment taskComment;

    @BeforeEach
    void setUp() {
        taskCommentService = new TaskCommentService(taskRepository, taskCommentRepository, taskCommentMapper);
        task = Task.builder()
                .id(88L)
                .uuid(UUID.randomUUID())
                .build();
        taskComment = TaskComment.builder()
                .taskId(task.getId())
                .task(task)
                .comment("North London forever")
                .build();
    }

    @Nested
    @DisplayName("Read Operations")
    class ReadOperations {
        @Test
        void getTaskCommentListItemsByTaskUuid_returnsList() {
            when(taskCommentRepository.findTaskCommentsListItemsByTaskUuid(task.getUuid()))
                    .thenReturn(List.of(
                            TaskCommentResponse.ListItem.builder().build(),
                            TaskCommentResponse.ListItem.builder().build()));

            assertThat(taskCommentService.getTaskCommentsListItemsByTaskUuid(task.getUuid())).hasSize(2);
            verify(taskCommentRepository).findTaskCommentsListItemsByTaskUuid(task.getUuid());
        }
    }

    @Nested
    @DisplayName("Create Operations")
    class CreateOperations {
        @Test
        void createTaskComment_savesTaskComment() {
            when(taskRepository.findByUuid(task.getUuid()))
                    .thenReturn(Optional.of(task));
            when(taskCommentRepository.save(taskComment))
                    .thenReturn(taskComment);

            TaskComment result = taskCommentService.createTaskComment(task.getUuid(), taskComment);

            assertThat(result).isEqualTo(taskComment);
            assertThat(result.getTask()).isEqualTo(task);
            verify(taskRepository).findByUuid(task.getUuid());
            verify(taskCommentRepository).save(taskComment);
        }

        @Test
        void createTaskComment_withNull_throwsException() {
            assertThatThrownBy(() -> taskCommentService.createTaskComment(task.getUuid(), null))
                    .isInstanceOf(DataValidationException.class);
        }

        @Test
        void createTaskComment_noTaskUuid_throwsException() {
            assertThatThrownBy(() -> taskCommentService.createTaskComment(null, taskComment))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Update Operations")
    class UpdateOperations {
        @Test
        void updateTaskComment_found_returnsTaskComment() {
            TaskComment updateTaskComment = new TaskComment();

            when(taskCommentRepository.findByUuid(taskComment.getUuid()))
                    .thenReturn(Optional.of(taskComment));
            when(taskCommentRepository.save(taskComment))
                    .thenReturn(taskComment);

            TaskComment result = taskCommentService.updateTaskComment(taskComment.getUuid(), updateTaskComment);

            assertThat(result).isEqualTo(taskComment);
            verify(taskCommentMapper).update(updateTaskComment, taskComment);
            verify(taskCommentRepository).save(taskComment);
        }

        @Test
        void updateTaskComment_notFound_throwsException() {
            when(taskCommentRepository.findByUuid(taskComment.getUuid()))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskCommentService.updateTaskComment(taskComment.getUuid(), taskComment))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Delete Operations")
    class DeleteOperations {
        @Test
        void deleteTaskComment_found_deletesTaskComment() {
            when(taskCommentRepository.findByUuid(taskComment.getUuid()))
                    .thenReturn(Optional.of(taskComment));

            taskCommentService.deleteTaskComment(taskComment.getUuid());

            verify(taskCommentRepository).delete(taskComment);
        }

        @Test
        void deleteTask_notFound_throwsException() {
            when(taskCommentRepository.findByUuid(taskComment.getUuid()))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskCommentService.deleteTaskComment(taskComment.getUuid()))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }
}
