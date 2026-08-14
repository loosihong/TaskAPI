package com.example.TaskAPI.web;

import com.example.TaskAPI.auth.service.AuthService;
import com.example.TaskAPI.core.BaseDatabaseCleanup;
import com.example.TaskAPI.core.SyncAuditExecutorConfig;
import com.example.TaskAPI.infrastructure.config.TestcontainersConfig;
import com.example.TaskAPI.user.domain.entity.User;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import(SyncAuditExecutorConfig.class)
public abstract class BaseIntegrationTest extends TestcontainersConfig {
    protected static String testUserToken;
    protected static User loginUser;
    private final List<Long> deleteUserIds = new ArrayList<>();
    @Autowired
    protected AuthService authService;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    private BaseDatabaseCleanup databaseCleanup;

    @BeforeAll
    void setUpData(@Autowired AuthService authService) {
        String username = "testuser";
        String password = "secret123";

        loginUser = authService.register(username, password);
        testUserToken = authService.login(username, password).getToken();
    }

    @AfterEach
    void cleanUp() {
        databaseCleanup.execute();
        databaseCleanup.deleteUsers(deleteUserIds);
        deleteUserIds.clear();
    }

    @AfterAll
    void cleanUpAuth() {
        databaseCleanup.deleteUsers(List.of(loginUser.getId()));
    }

    protected User createUser(String username) {
        User user = authService.register(username, "password");
        deleteUserIds.add(user.getId());
        return user;
    }
}
