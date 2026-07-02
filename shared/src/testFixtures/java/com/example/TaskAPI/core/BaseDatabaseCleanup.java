package com.example.TaskAPI.core;

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
public abstract class BaseDatabaseCleanup {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private EntityManagerFactory entityManagerFactory;
    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    protected abstract List<Class<?>> entityClasses();

    protected abstract String userTableName();

    public void execute() {
        entityClasses().forEach(entityClass -> {
            String tableName = resolveTableName(entityClass);
            jdbcTemplate.execute("DELETE FROM " + tableName);
        });
    }

    public void deleteUsers(List<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return;
        }

        namedParameterJdbcTemplate.update("DELETE FROM %s WHERE id IN (:ids)"
                        .formatted(userTableName()),
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
