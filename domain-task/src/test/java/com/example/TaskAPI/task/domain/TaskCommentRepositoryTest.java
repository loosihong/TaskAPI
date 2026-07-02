package com.example.TaskAPI.task.domain;

import com.example.TaskAPI.core.BaseEntityRepositoryTest;
import com.example.TaskAPI.task.domain.entity.Task;
import com.example.TaskAPI.task.domain.entity.TaskComment;
import com.example.TaskAPI.task.domain.repository.TaskCommentRepository;
import com.example.TaskAPI.task.domain.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class TaskCommentRepositoryTest extends BaseEntityRepositoryTest<TaskComment> {
    @Autowired
    private TaskCommentRepository taskCommentRepository;

    @Autowired
    private TaskRepository taskRepository;

    private TaskComment taskComment;

    @BeforeEach
    void setup() {
        Task task = Task.builder()
                .title("Buy groceries")
                .status("TODO")
                .build();

        taskRepository.saveAndFlush(task);
        taskComment = TaskComment.builder()
                .comment("procrastinating")
                .task(task)
                .build();
        taskCommentRepository.saveAndFlush(taskComment);
        entityManager.clear();
    }

    @Test
    void create_defaultData() {
        assertCreationData(taskComment);
    }

    @Test
    void save_throwsOnVersionConflict() {
        assertVersionConflict(taskComment, taskCommentRepository);
    }

    @Test
    void deleteById_performsSoftDelete() {
        assertSoftDeleteById(taskComment, taskCommentRepository);
    }

    @Test
    void deleteByUuid_performsSoftDelete() {
        assertSoftDeleteByUuid(taskComment, taskCommentRepository);
    }
}
