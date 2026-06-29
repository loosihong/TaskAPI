package com.example.TaskAPI.core;

import com.example.TaskAPI.infrastructure.config.AuditTestConfig;
import com.example.TaskAPI.infrastructure.config.JpaConfig;
import com.example.TaskAPI.infrastructure.config.TestcontainersConfig;
import com.example.TaskAPI.user.domain.entity.User;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

@Import({AuditTestConfig.class, JpaConfig.class})
@DataJpaTest
public abstract class BaseRepositoryTest extends TestcontainersConfig {
    @Autowired
    protected TestEntityManager entityManager;
    @Autowired
    protected JPAQueryFactory jpaQueryFactory;

    protected User createUser(String username) {
        return entityManager.persistAndFlush(User.builder()
                .username(username)
                .password("password")
                .build());
    }
}
