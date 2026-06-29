package com.example.TaskAPI.task.domain.repository;

import com.example.TaskAPI.core.model.repository.BaseEntityRepository;
import com.example.TaskAPI.task.domain.entity.QTask;
import com.example.TaskAPI.task.domain.entity.Task;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.core.types.dsl.StringPath;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.querydsl.binding.SingleValueBinding;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskRepository extends
        BaseEntityRepository<Task>,
        QuerydslPredicateExecutor<Task>,
        QuerydslBinderCustomizer<QTask>,
        TaskRepositoryDashboard {
    @EntityGraph(attributePaths = {"taskDetail", "taskAssignees", "taskAssignees.user"})
    Optional<Task> findWithDetailByUuid(UUID uuid);

    @EntityGraph(attributePaths = {"taskAssignees"})
    Optional<Task> findWithAssigneesByUuid(UUID uuid);

    @EntityGraph(attributePaths = {"taskAssignees", "taskAssignees.user"})
    @Override
    List<Task> findAll();

    @Override
    default void customize(QuerydslBindings bindings, QTask root) {
        bindings.bind(String.class)
                .first((SingleValueBinding<StringPath, String>) StringExpression::containsIgnoreCase);
        bindings.excluding(root.id, root.version, root.deleted);
    }
}
