package com.example.TaskAPI.task.domain.query;

import com.example.TaskAPI.core.exception.DataValidationException;
import com.example.TaskAPI.task.domain.entity.QTask;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class TaskPredicateBuilder {
    public Predicate buildPredicate(TaskListFilter filter) {
        if (filter.createdAtFrom() != null
                && filter.createdAtTo() != null
                && filter.createdAtFrom().isAfter(filter.createdAtTo())) {
            throw new DataValidationException(TaskListFilter.class,
                    TaskListFilter.Fields.createdAtFrom + " cannot be after " + TaskListFilter.Fields.createdAtTo);
        }

        QTask task = QTask.task;
        BooleanBuilder builder = new BooleanBuilder();

        if (filter.title() != null && !filter.title().isBlank()) {
            builder.and(task.title.containsIgnoreCase(filter.title().strip()));
        }

        if (!CollectionUtils.isEmpty(filter.statuses())) {
            builder.and(task.status.in(filter.statuses()));
        }

        if (!CollectionUtils.isEmpty(filter.assigneeUuids())) {
            builder.and(task.taskAssignees.any().user.uuid.in(filter.assigneeUuids()));
        }

        if (filter.createdAtFrom() != null) {
            builder.and(task.createdAt.goe(filter.createdAtFrom()));
        }

        if (filter.createdAtTo() != null) {
            builder.and(task.createdAt.loe(filter.createdAtTo()));
        }

        return builder;
    }
}
