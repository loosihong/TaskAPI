package com.example.TaskAPI.task.domain;

import com.example.TaskAPI.core.BaseEntityRepositoryTest;
import com.example.TaskAPI.core.exception.DataValidationException;
import com.example.TaskAPI.core.model.BaseEntity;
import com.example.TaskAPI.infrastructure.config.AuditTestConfig;
import com.example.TaskAPI.task.api.dto.TaskDashboardFilter;
import com.example.TaskAPI.task.api.dto.TaskDashboardItem;
import com.example.TaskAPI.task.api.dto.TaskListFilter;
import com.example.TaskAPI.task.domain.entity.QTask;
import com.example.TaskAPI.task.domain.entity.Task;
import com.example.TaskAPI.task.domain.entity.TaskDetail;
import com.example.TaskAPI.task.domain.enums.Priority;
import com.example.TaskAPI.task.domain.query.TaskPredicateBuilder;
import com.example.TaskAPI.task.domain.repository.TaskRepository;
import com.example.TaskAPI.user.domain.entity.User;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Pair;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;

@Import(TaskPredicateBuilder.class)
public class TaskRepositoryTest extends BaseEntityRepositoryTest<Task> {

    @Autowired
    private TaskRepository taskRepository;
    private Task InitialTask;
    @Autowired
    private TaskPredicateBuilder taskPredicateBuilder;

    @BeforeEach
    void setupEach() {
        InitialTask = Task.builder()
                .title("Buy groceries")
                .status("TODO")
                .createdAt(LocalDateTime.now())
                .build();
        taskRepository.saveAndFlush(InitialTask);
        entityManager.clear();
    }

    @Nested
    @DisplayName("TaskRepository base")
    class TestTaskRepository {
        @Test
        void create_defaultData() {
            assertCreationData(InitialTask);
        }

        @Test
        void save_throwsOnVersionConflict() {
            assertVersionConflict(InitialTask, taskRepository);
        }

        @Test
        void deleteById_performsSoftDelete() {
            assertSoftDeleteById(InitialTask, taskRepository);
        }

        @Test
        void deleteByUuid_performsSoftDelete() {
            assertSoftDeleteByUuid(InitialTask, taskRepository);
        }

        @Test
        void updateAuditableField_shouldCreateAuditLog() {
            assertAuditableField(InitialTask, taskRepository, List.of(
                    Pair.of(Task.Fields.status, "COMPLETED")
            ));
        }

        @Test
        void updateNonAuditableField_shouldNotCreateAuditLog() {
            assertNonAuditableField(InitialTask, taskRepository, List.of(
                    Pair.of(Task.Fields.title, "New title")
            ));
        }
    }

    @Nested
    @DisplayName("Search TaskList")
    class SearchTaskList {
        private Task createTask(String title, String status) {
            return entityManager.persistAndFlush(Task.builder()
                    .title(title)
                    .status(status)
                    .build());
        }

        private void setCreatedAt(Task task, LocalDateTime createdAt) {
            QTask qTask = QTask.task;

            jpaQueryFactory
                    .update(qTask)
                    .set(qTask.createdAt, createdAt)
                    .where(QTask.task.id.eq(task.getId()))
                    .execute();
            entityManager.refresh(task);
        }

        private PageRequest getDefaultPageRequest() {
            return PageRequest.of(0, 20, Sort.by(DESC, BaseEntity.Fields.createdAt));
        }

        @Test
        void searchTasks_noFilter_returnsAllTasks() {
            Task task1 = createTask("Read book", "DONE");

            TaskListFilter taskListFilter = TaskListFilter.builder().build();
            Page<Task> result = taskRepository.findAll(
                    taskPredicateBuilder.buildPredicate(taskListFilter), getDefaultPageRequest());

            assertThat(result.getTotalElements()).isEqualTo(2);
        }

        @Test
        void searchTasks_title_returnsTasks() {
            Task task1 = createTask("North London", "DONE");

            TaskListFilter taskListFilter = TaskListFilter.builder()
                    .title(task1.getTitle().toLowerCase().substring(1))
                    .build();
            Page<Task> result = taskRepository.findAll(
                    taskPredicateBuilder.buildPredicate(taskListFilter), getDefaultPageRequest());

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getTitle()).isEqualTo(task1.getTitle());
        }

        @Test
        void searchTasks_blankTitle_returnsAllTasks() {
            Task task = createTask("Read book", "DONE");

            TaskListFilter taskListFilter = TaskListFilter.builder()
                    .title("   ")
                    .build();
            Page<Task> result = taskRepository.findAll(
                    taskPredicateBuilder.buildPredicate(taskListFilter), getDefaultPageRequest());

            assertThat(result.getTotalElements()).isEqualTo(2);
        }

        @Test
        void searchTasks_statuses_returnsTasks() {
            Task task1 = createTask("North London", "DONE");
            Task task2 = createTask("Arsenal forever", "COMPLETED");

            TaskListFilter taskListFilter = TaskListFilter.builder()
                    .statuses(List.of(task1.getStatus(), task2.getStatus()))
                    .build();
            Page<Task> result = taskRepository.findAll(
                    taskPredicateBuilder.buildPredicate(taskListFilter), getDefaultPageRequest());

            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getContent())
                    .extracting(Task::getStatus)
                    .containsExactlyInAnyOrder(task1.getStatus(), task2.getStatus());
        }

        @Test
        void searchTasks_createdAt_returnsTasks() {
            Task task1 = createTask("North London", "DONE");
            Task task2 = createTask("Arsenal forever", "COMPLETED");

            setCreatedAt(task1, LocalDateTime.now().minusDays(7));
            setCreatedAt(task2, LocalDateTime.now().minusDays(3));

            TaskListFilter taskListFilter = TaskListFilter.builder()
                    .createdAtFrom(LocalDateTime.now().minusDays(10))
                    .createdAtTo(LocalDateTime.now().minusDays(3))
                    .build();
            Page<Task> result = taskRepository.findAll(
                    taskPredicateBuilder.buildPredicate(taskListFilter), getDefaultPageRequest());

            assertThat(result.getContent()).hasSize(2);
        }

        @Test
        void searchTasks_pagination_returnsPage() {
            IntStream.rangeClosed(1, 20)
                    .forEach(idx -> createTask("Task " + idx, "TODO"));

            TaskListFilter taskListFilter = TaskListFilter.builder().build();
            PageRequest pageRequest = PageRequest.of(1, 5,
                    Sort.by(ASC, BaseEntity.Fields.createdAt));
            Page<Task> result = taskRepository.findAll(
                    taskPredicateBuilder.buildPredicate(taskListFilter), pageRequest);

            assertThat(result.getContent()).hasSize(5);
            assertThat(result.getTotalElements()).isEqualTo(21);
            assertThat(result.getTotalPages()).isEqualTo(5);
        }

        @Test
        void searchTasks_sortTitle_returnsSortedResult() {
            Task task1 = createTask("Alpha task", "TODO");
            Task task2 = createTask("Beta task", "TODO");

            TaskListFilter taskListFilter = TaskListFilter.builder().build();
            PageRequest pageRequest = PageRequest.of(0, 20, Sort.by(ASC, Task.Fields.title));
            Page<Task> result = taskRepository.findAll(
                    taskPredicateBuilder.buildPredicate(taskListFilter), pageRequest);

            assertThat(result.getContent())
                    .extracting(Task::getTitle)
                    .containsExactly(task1.getTitle(), task2.getTitle(), InitialTask.getTitle());
        }

        @Test
        void searchTasks_noResult_returnsEmptyPage() {
            TaskListFilter taskListFilter = TaskListFilter.builder()
                    .statuses(List.of("INVALID"))
                    .build();
            Page<Task> result = taskRepository.findAll(
                    taskPredicateBuilder.buildPredicate(taskListFilter), getDefaultPageRequest());

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }

        @Test
        void searchTasks_invalidFilter_returnsException() {
            TaskListFilter taskListFilter = TaskListFilter.builder()
                    .createdAtFrom(LocalDateTime.now())
                    .createdAtTo(LocalDateTime.now().minusDays(1))
                    .build();

            assertThatThrownBy(() -> taskRepository.findAll(
                    taskPredicateBuilder.buildPredicate(taskListFilter), getDefaultPageRequest()))
                    .isInstanceOf(DataValidationException.class);
        }
    }

    @Nested
    @DisplayName("Search TaskDashboard")
    class SearchTaskDashboard {
        private User assignee;

        private PageRequest getDefaultPageRequest() {
            return PageRequest.of(0, 20, Sort.by(DESC, TaskDashboardItem.Fields.updatedAt));
        }

        @BeforeEach
        void setupData() {
            assignee = createUser("user1");
            AuditTestConfig.setCurrentAuditor(assignee.getId());
        }

        @AfterEach
        void tearDown() {
            AuditTestConfig.resetCurrentAuditor();
        }

        private Task createTask(
                String title,
                String status,
                List<User> assignees,
                Priority priority
        ) {
            Task task = Task.builder()
                    .title(title)
                    .status(status)
                    .build();

            task.setTaskDetail(TaskDetail.builder()
                    .priority(priority)
                    .build());

            if (assignees != null) {
                for (User user : assignees) {
                    task.addAssignee(user);
                }
            }

            entityManager.persistAndFlush(task);

            return task;
        }

        private void setCreatedAt(Task task, LocalDateTime createdAt) {
            QTask qTask = QTask.task;

            jpaQueryFactory
                    .update(qTask)
                    .set(qTask.createdAt, createdAt)
                    .where(qTask.id.eq(task.getId()))
                    .execute();
            entityManager.refresh(task);
        }

        private void setCreatedBy(Task task, long createdBy) {
            QTask qTask = QTask.task;

            jpaQueryFactory
                    .update(qTask)
                    .set(qTask.createdBy, createdBy)
                    .where(qTask.id.eq(task.getId()))
                    .execute();
            entityManager.refresh(task);
        }

        private void setUpdatedAt(Task task, LocalDateTime updatedAt) {
            QTask qTask = QTask.task;

            jpaQueryFactory
                    .update(qTask)
                    .set(qTask.updatedAt, updatedAt)
                    .where(qTask.id.eq(task.getId()))
                    .execute();
            entityManager.refresh(task);
        }

        private void setUpdatedBy(Task task, Long updatedBy) {
            QTask qTask = QTask.task;

            jpaQueryFactory
                    .update(qTask)
                    .set(qTask.updatedBy, updatedBy)
                    .where(qTask.id.eq(task.getId()))
                    .execute();
            entityManager.refresh(task);
        }

        @Test
        void searchTasks_noFilter_returnsAllDashboardItems() {
            Task task1 = createTask(
                    "Read book",
                    "TODO",
                    new ArrayList<>(List.of(assignee)),
                    Priority.LOW);
            Task task2 = createTask(
                    "Arsenal",
                    "TODO",
                    new ArrayList<>(List.of(assignee)),
                    Priority.LOW);

            Page<TaskDashboardItem> result = taskRepository.findTaskDashboardItems(
                    TaskDashboardFilter.builder().build(),
                    getDefaultPageRequest(),
                    assignee.getId());

            assertThat(result.getTotalElements()).isEqualTo(2);
        }

        @Test
        void searchTasks_title_returnDashboardItems() {
            Task task1 = createTask(
                    "Read book",
                    "TODO",
                    new ArrayList<>(List.of(assignee)),
                    Priority.LOW);

            Page<TaskDashboardItem> result = taskRepository.findTaskDashboardItems(
                    TaskDashboardFilter.builder()
                            .title(task1.getTitle())
                            .build(),
                    getDefaultPageRequest(),
                    assignee.getId());

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).title()).isEqualTo(task1.getTitle());
        }

        @Test
        void searchTasks_blankTitle_returnDashboardItems() {
            Task task1 = createTask(
                    "Read book",
                    "TODO",
                    new ArrayList<>(List.of(assignee)),
                    Priority.LOW);

            Page<TaskDashboardItem> result = taskRepository.findTaskDashboardItems(
                    TaskDashboardFilter.builder()
                            .title("   ")
                            .build(),
                    getDefaultPageRequest(),
                    assignee.getId());

            assertThat(result.getTotalElements()).isEqualTo(1);
        }

        @Test
        void searchTasks_statuses_returnDashboardItems() {
            Task task1 = createTask(
                    "Read book",
                    "DONE",
                    new ArrayList<>(List.of(assignee)),
                    Priority.LOW);
            Task task2 = createTask(
                    "North London forever",
                    "COMPLETED",
                    new ArrayList<>(List.of(assignee)),
                    Priority.LOW);

            Page<TaskDashboardItem> result = taskRepository.findTaskDashboardItems(
                    TaskDashboardFilter.builder()
                            .statuses(List.of(task1.getStatus(), task2.getStatus()))
                            .build(),
                    getDefaultPageRequest(),
                    assignee.getId());

            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result)
                    .extracting(TaskDashboardItem::status)
                    .containsExactlyInAnyOrder(task1.getStatus(), task2.getStatus());
        }

        @Test
        void searchTasks_priority_returnDashboardItems() {
            Task task1 = createTask(
                    "Read book",
                    "DONE",
                    new ArrayList<>(List.of(assignee)),
                    Priority.MEDIUM);
            Task task2 = createTask(
                    "North London forever",
                    "COMPLETED",
                    new ArrayList<>(List.of(assignee)),
                    Priority.HIGH);

            Page<TaskDashboardItem> result = taskRepository.findTaskDashboardItems(
                    TaskDashboardFilter.builder()
                            .priorities(List.of(Priority.MEDIUM, Priority.HIGH))
                            .build(),
                    getDefaultPageRequest(),
                    assignee.getId());

            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result)
                    .extracting(TaskDashboardItem::priority)
                    .containsExactlyInAnyOrder(Priority.MEDIUM, Priority.HIGH);
        }

        @Test
        void searchTasks_updatedAt_returnDashboardItems() {
            Task task1 = createTask(
                    "Read book",
                    "DONE",
                    new ArrayList<>(List.of(assignee)),
                    Priority.MEDIUM);
            Task task2 = createTask(
                    "North London forever",
                    "COMPLETED",
                    new ArrayList<>(List.of(assignee)),
                    Priority.HIGH);

            setUpdatedAt(task1, LocalDateTime.now().minusDays(7));
            setUpdatedAt(task2, LocalDateTime.now().minusDays(5));

            Page<TaskDashboardItem> result = taskRepository.findTaskDashboardItems(
                    TaskDashboardFilter.builder()
                            .updatedAtFrom(LocalDateTime.now().minusDays(10))
                            .updatedAtTo(LocalDateTime.now().minusDays(3))
                            .build(),
                    getDefaultPageRequest(),
                    assignee.getId());

            assertThat(result.getTotalElements()).isEqualTo(2);
        }

        @Test
        void searchTasks_updatedUuids_returnDashboardItems() {
            Task task1 = createTask(
                    "Read book",
                    "TODO",
                    new ArrayList<>(List.of(assignee)),
                    Priority.LOW);
            Task task2 = createTask(
                    "Arsenal",
                    "TODO",
                    new ArrayList<>(List.of(assignee)),
                    Priority.LOW);

            Page<TaskDashboardItem> result = taskRepository.findTaskDashboardItems(
                    TaskDashboardFilter.builder()
                            .updatedByUuids(List.of(assignee.getUuid()))
                            .build(),
                    getDefaultPageRequest(),
                    assignee.getId());

            assertThat(result.getTotalElements()).isEqualTo(2);
        }

        @Test
        void searchTasks_pagination_returnsPage() {
            IntStream.rangeClosed(0, 20)
                    .forEach(idx -> createTask(
                            "Task " + idx,
                            "TODO",
                            List.of(assignee),
                            Priority.MEDIUM));

            Page<TaskDashboardItem> result = taskRepository.findTaskDashboardItems(
                    TaskDashboardFilter.builder().build(),
                    PageRequest.of(1, 5,
                            Sort.by(ASC, TaskDashboardItem.Fields.createdAt)),
                    assignee.getId());

            assertThat(result.getContent()).hasSize(5);
            assertThat(result.getTotalElements()).isEqualTo(21);
            assertThat(result.getTotalPages()).isEqualTo(5);
        }

        @Test
        void searchTasks_sortTitle_returnsSortedResult() {
            Task task1 = createTask(
                    "Task C",
                    "TODO",
                    List.of(assignee),
                    Priority.LOW);
            Task task2 = createTask(
                    "Task A",
                    "TODO",
                    List.of(assignee),
                    Priority.LOW);
            Task task3 = createTask(
                    "Task B",
                    "TODO",
                    List.of(assignee),
                    Priority.LOW);

            Page<TaskDashboardItem> result = taskRepository.findTaskDashboardItems(
                    TaskDashboardFilter.builder().build(),
                    PageRequest.of(0, 20,
                            Sort.by(ASC, TaskDashboardItem.Fields.title)),
                    assignee.getId());

            assertThat(result.getContent())
                    .extracting(TaskDashboardItem::title)
                    .containsExactly(task2.getTitle(), task3.getTitle(), task1.getTitle());
        }

        @Test
        void searchTasks_sortStatus_returnsSortedResult() {
            Task task1 = createTask(
                    "Task C",
                    "STAT_X",
                    List.of(assignee),
                    Priority.LOW);
            Task task2 = createTask(
                    "Task A",
                    "STAT_Z",
                    List.of(assignee),
                    Priority.LOW);
            Task task3 = createTask(
                    "Task B",
                    "STAT_Y",
                    List.of(assignee),
                    Priority.LOW);

            Page<TaskDashboardItem> result = taskRepository.findTaskDashboardItems(
                    TaskDashboardFilter.builder().build(),
                    PageRequest.of(0, 20,
                            Sort.by(DESC, TaskDashboardItem.Fields.status)),
                    assignee.getId());

            assertThat(result.getContent())
                    .extracting(TaskDashboardItem::status)
                    .containsExactly(task2.getStatus(), task3.getStatus(), task1.getStatus());
        }

        @Test
        void searchTasks_sortCreatedAt_returnsSortedResult() {
            Task task1 = createTask(
                    "Task C",
                    "TODO",
                    List.of(assignee),
                    Priority.LOW);
            Task task2 = createTask(
                    "Task A",
                    "TODO",
                    List.of(assignee),
                    Priority.MEDIUM);
            Task task3 = createTask(
                    "Task B",
                    "TODO",
                    List.of(assignee),
                    Priority.HIGH);

            setCreatedAt(task2, LocalDateTime.now().minusDays(1));
            setCreatedAt(task3, LocalDateTime.now().minusDays(2));

            Page<TaskDashboardItem> result = taskRepository.findTaskDashboardItems(
                    TaskDashboardFilter.builder().build(),
                    PageRequest.of(0, 20,
                            Sort.by(DESC, TaskDashboardItem.Fields.createdAt)),
                    assignee.getId());

            assertThat(result.getContent())
                    .extracting(TaskDashboardItem::createdAt)
                    .containsExactly(
                            task1.getCreatedAt(),
                            task2.getCreatedAt(),
                            task3.getCreatedAt());
        }

        @Test
        void searchTasks_sortCreatedBy_returnsSortedResult() {
            Task task1 = createTask(
                    "Task C",
                    "TODO",
                    List.of(assignee),
                    Priority.LOW);
            Task task2 = createTask(
                    "Task A",
                    "TODO",
                    List.of(assignee),
                    Priority.MEDIUM);
            Task task3 = createTask(
                    "Task B",
                    "TODO",
                    List.of(assignee),
                    Priority.HIGH);
            User user2 = createUser("user2");
            User user3 = createUser("user3");

            setCreatedBy(task1, user2.getId());
            setCreatedBy(task2, user3.getId());

            Page<TaskDashboardItem> result = taskRepository.findTaskDashboardItems(
                    TaskDashboardFilter.builder().build(),
                    PageRequest.of(0, 20,
                            Sort.by(ASC, TaskDashboardItem.Fields.createdByName)),
                    assignee.getId());

            assertThat(result.getContent())
                    .extracting(TaskDashboardItem::createdByName)
                    .containsExactly(
                            assignee.getUsername(),
                            user2.getUsername(),
                            user3.getUsername());
        }

        @Test
        void searchTasks_sortUpdatedAt_returnsSortedResult() {
            Task task1 = createTask(
                    "Task C",
                    "TODO",
                    List.of(assignee),
                    Priority.LOW);
            Task task2 = createTask(
                    "Task A",
                    "TODO",
                    List.of(assignee),
                    Priority.MEDIUM);
            Task task3 = createTask(
                    "Task B",
                    "TODO",
                    List.of(assignee),
                    Priority.HIGH);

            setUpdatedAt(task2, LocalDateTime.now().minusDays(1));
            setUpdatedAt(task3, LocalDateTime.now().minusDays(2));

            Page<TaskDashboardItem> result = taskRepository.findTaskDashboardItems(
                    TaskDashboardFilter.builder().build(),
                    PageRequest.of(0, 20,
                            Sort.by(DESC, TaskDashboardItem.Fields.updatedAt)),
                    assignee.getId());

            assertThat(result.getContent())
                    .extracting(TaskDashboardItem::updatedAt)
                    .containsExactly(
                            task1.getUpdatedAt(),
                            task2.getUpdatedAt(),
                            task3.getUpdatedAt());
        }

        @Test
        void searchTasks_sortUpdatedBy_returnsSortedResult() {
            Task task1 = createTask(
                    "Task C",
                    "TODO",
                    List.of(assignee),
                    Priority.LOW);
            Task task2 = createTask(
                    "Task A",
                    "TODO",
                    List.of(assignee),
                    Priority.MEDIUM);
            Task task3 = createTask(
                    "Task B",
                    "TODO",
                    List.of(assignee),
                    Priority.HIGH);
            User user2 = createUser("user2");
            User user3 = createUser("user3");

            setUpdatedBy(task3, user2.getId());
            setUpdatedBy(task2, user3.getId());

            Page<TaskDashboardItem> result = taskRepository.findTaskDashboardItems(
                    TaskDashboardFilter.builder().build(),
                    PageRequest.of(0, 20,
                            Sort.by(DESC, TaskDashboardItem.Fields.updatedByName)),
                    assignee.getId());

            assertThat(result.getContent())
                    .extracting(TaskDashboardItem::updatedByName)
                    .containsExactly(
                            user3.getUsername(),
                            user2.getUsername(),
                            assignee.getUsername());
        }

        @Test
        void searchTasks_sortPriority_returnsSortedResult() {
            Task task1 = createTask(
                    "Task C",
                    "TODO",
                    List.of(assignee),
                    Priority.LOW);
            Task task2 = createTask(
                    "Task A",
                    "TODO",
                    List.of(assignee),
                    Priority.MEDIUM);
            Task task3 = createTask(
                    "Task B",
                    "TODO",
                    List.of(assignee),
                    Priority.HIGH);

            Page<TaskDashboardItem> result = taskRepository.findTaskDashboardItems(
                    TaskDashboardFilter.builder().build(),
                    PageRequest.of(0, 20,
                            Sort.by(DESC, TaskDashboardItem.Fields.priority)),
                    assignee.getId());

            assertThat(result.getContent())
                    .extracting(TaskDashboardItem::priority)
                    .containsExactly(
                            task3.getTaskDetail().getPriority(),
                            task2.getTaskDetail().getPriority(),
                            task1.getTaskDetail().getPriority());
        }

        @Test
        void searchTasks_noFilter_returnsSortedAssigneeNames() {
            User user2 = createUser("user2");
            User user3 = createUser("user3");
            Task task = createTask(
                    "Read book",
                    "TODO",
                    new ArrayList<>(List.of(user2, user3, assignee)),
                    Priority.LOW);

            Page<TaskDashboardItem> result = taskRepository.findTaskDashboardItems(
                    TaskDashboardFilter.builder().build(),
                    getDefaultPageRequest(),
                    assignee.getId());

            assertThat(result.getContent())
                    .singleElement()
                    .satisfies(item -> {
                        assertThat(item.taskUuid()).isEqualTo(task.getUuid());
                        assertThat(item.assigneeNames()).isEqualTo("user1, user2, user3");
                    });
        }

        @Test
        void searchTasks_noResult_returnsEmptyPage() {
            Page<TaskDashboardItem> result = taskRepository.findTaskDashboardItems(
                    TaskDashboardFilter.builder()
                            .statuses(List.of("INVALID"))
                            .build(),
                    getDefaultPageRequest(),
                    assignee.getId());

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }

        @Test
        void searchTasks_invalidFilter_returnsException() {
            TaskDashboardFilter taskDashboardFilter = TaskDashboardFilter.builder()
                    .updatedAtFrom(LocalDateTime.now())
                    .updatedAtTo(LocalDateTime.now().minusDays(1))
                    .build();

            assertThatThrownBy(() -> taskRepository.findTaskDashboardItems(
                    taskDashboardFilter,
                    getDefaultPageRequest(),
                    assignee.getId()))
                    .isInstanceOf(DataValidationException.class);
        }
    }
}
