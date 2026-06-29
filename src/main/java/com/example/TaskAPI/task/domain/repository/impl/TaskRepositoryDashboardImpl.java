package com.example.TaskAPI.task.domain.repository.impl;

import com.example.TaskAPI.core.exception.DataValidationException;
import com.example.TaskAPI.core.model.BaseEntity;
import com.example.TaskAPI.task.api.dto.TaskDashboardFilter;
import com.example.TaskAPI.task.api.dto.TaskDashboardItem;
import com.example.TaskAPI.task.domain.entity.QTask;
import com.example.TaskAPI.task.domain.entity.QTaskAssignee;
import com.example.TaskAPI.task.domain.entity.QTaskDetail;
import com.example.TaskAPI.task.domain.repository.TaskRepositoryDashboard;
import com.example.TaskAPI.user.domain.entity.QUser;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.CollectionUtils;

import java.util.List;

@RequiredArgsConstructor
public class TaskRepositoryDashboardImpl implements TaskRepositoryDashboard {
    private final JPAQueryFactory jpaQueryFactory;
    private final QTask task = QTask.task;
    private final QTaskDetail taskDetail = QTaskDetail.taskDetail;
    private final QTaskAssignee taskAssignee = QTaskAssignee.taskAssignee;
    private final QUser creator = new QUser("createdBy");
    private final QUser updater = new QUser("updatedBy");

    @Override
    public Page<TaskDashboardItem> findTaskDashboardItems(
            TaskDashboardFilter taskDashboardFilter,
            Pageable pageable,
            Long userId
    ) {
        if (taskDashboardFilter == null
                || pageable == null
                || userId == null) {
            throw new IllegalArgumentException("All params are required.");
        }

        QTaskAssignee subTaskAssignee = new QTaskAssignee("subTaskAssignee");
        QUser subUser = new QUser("subUser");
        Predicate predicate = buildPredicate(taskDashboardFilter, userId);
        OrderSpecifier<?> order = buildOrder(pageable.getSort());

        JPQLQuery<String> assigneeNamesSubQuery = JPAExpressions
                .select(Expressions.stringTemplate(
                        "COALESCE(LISTAGG({0}, ', ') WITHIN GROUP(ORDER BY {1}), '')",
                        subUser.username,
                        subUser.username))
                .from(subTaskAssignee)
                .join(subTaskAssignee.user, subUser)
                .where(subTaskAssignee.task.id.eq(task.id));
        List<TaskDashboardItem> result = jpaQueryFactory
                .select(Projections.constructor(TaskDashboardItem.class,
                        task.uuid,
                        task.title,
                        task.status,
                        task.createdAt,
                        creator.username,
                        task.updatedAt,
                        updater.username,
                        taskDetail.priority,
                        assigneeNamesSubQuery
                ))
                .from(task)
                .innerJoin(taskDetail).on(taskDetail.task.id.eq(task.id))
                .innerJoin(taskAssignee).on(taskAssignee.task.id.eq(task.id))
                .leftJoin(creator).on(creator.id.eq(task.createdBy))
                .leftJoin(updater).on(updater.id.eq(task.updatedBy))
                .where(predicate)
                .orderBy(order)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        long totalCount = jpaQueryFactory.select(task.count())
                .from(task)
                .innerJoin(taskDetail).on(taskDetail.task.id.eq(task.id))
                .innerJoin(taskAssignee).on(taskAssignee.task.id.eq(task.id))
                .leftJoin(updater).on(updater.id.eq(task.updatedBy))
                .where(predicate)
                .fetchOne();

        return new PageImpl<>(result, pageable, totalCount);
    }

    private Predicate buildPredicate(TaskDashboardFilter filter, Long userId) {
        if (filter == null
                || userId == null) {
            throw new IllegalArgumentException("All params are required.");
        }

        if (filter.updatedAtFrom() != null
                && filter.updatedAtTo() != null
                && filter.updatedAtFrom().isAfter(filter.updatedAtTo())) {
            throw new DataValidationException(TaskDashboardFilter.class,
                    TaskDashboardFilter.Fields.updatedAtFrom + " cannot be after " + TaskDashboardFilter.Fields.updatedAtTo);
        }

        BooleanBuilder builder = new BooleanBuilder();

        builder.and(taskAssignee.user.id.eq(userId));
        builder.and(task.deleted.isFalse());

        if (filter.title() != null && !filter.title().isBlank()) {
            builder.and(task.title.containsIgnoreCase(filter.title().strip()));
        }

        if (!CollectionUtils.isEmpty(filter.statuses())) {
            builder.and(task.status.in(filter.statuses()));
        }

        if (!CollectionUtils.isEmpty(filter.priorities())) {
            builder.and(taskDetail.priority.in(filter.priorities()));
        }

        if (filter.updatedAtFrom() != null) {
            builder.and(task.updatedAt.goe(filter.updatedAtFrom()));
        }

        if (filter.updatedAtTo() != null) {
            builder.and(task.updatedAt.loe(filter.updatedAtTo()));
        }

        if (!CollectionUtils.isEmpty(filter.updatedByUuids())) {
            builder.and(updater.uuid.in(filter.updatedByUuids()));
        }

        return builder;
    }

    private OrderSpecifier<?> buildOrder(Sort sort) {
        Sort.Order order = sort.stream().findFirst().orElse(Sort.Order.desc(BaseEntity.Fields.updatedAt));
        Order direction = (order.isAscending() ? Order.ASC : Order.DESC);

        return switch (order.getProperty()) {
            case TaskDashboardItem.Fields.title -> new OrderSpecifier<>(direction, task.title);
            case TaskDashboardItem.Fields.status -> new OrderSpecifier<>(direction, task.status);
            case TaskDashboardItem.Fields.createdAt -> new OrderSpecifier<>(direction, task.createdAt);
            case TaskDashboardItem.Fields.createdByName -> new OrderSpecifier<>(direction, creator.username);
            case TaskDashboardItem.Fields.updatedByName -> new OrderSpecifier<>(direction, updater.username);
            case TaskDashboardItem.Fields.priority -> new OrderSpecifier<>(direction, taskDetail.priority);
            default -> new OrderSpecifier<>(direction, task.updatedAt);
        };
    }
}
