package com.example.TaskAPI.infrastructure.config;

import com.example.TaskAPI.core.audit.AuditFieldLog;
import com.example.TaskAPI.core.audit.AuditLog;
import com.example.TaskAPI.task.domain.entity.Task;
import com.example.TaskAPI.task.domain.entity.TaskAssignee;
import com.example.TaskAPI.task.domain.entity.TaskComment;
import com.example.TaskAPI.task.domain.entity.TaskDetail;
import com.example.TaskAPI.user.domain.entity.User;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

@Component
public class DatabaseCleanup {
    private static final List<Class<?>> ENTITY_CLASSES = List.of(
            TaskAssignee.class,
            TaskComment.class,
            TaskDetail.class,
            Task.class,
            AuditFieldLog.class,
            AuditLog.class
    );
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private EntityManagerFactory entityManagerFactory;
    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public void execute() {
        ENTITY_CLASSES.forEach(entityClass -> {
            String tableName = resolveTableName(entityClass);
            jdbcTemplate.execute("DELETE FROM " + tableName);
        });
    }

    public void deleteUsers(List<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return;
        }

        namedParameterJdbcTemplate.update("DELETE FROM %s WHERE id IN (:ids)"
                        .formatted(User.Reference.TABLE_NAME),
                Map.of("ids", userIds));
    }

    private String resolveTableName(Class<?> entityClass) {
        SessionFactoryImplementor sessionFactory = entityManagerFactory
                .unwrap(SessionFactoryImplementor.class);

        return sessionFactory
                .getRuntimeMetamodels()
                .getMappingMetamodel()
                .getEntityDescriptor(entityClass)
                .getTableName();
    }
}
