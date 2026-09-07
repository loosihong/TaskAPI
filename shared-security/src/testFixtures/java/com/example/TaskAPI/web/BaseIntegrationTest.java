package com.example.TaskAPI.web;

import com.example.TaskAPI.core.BaseDatabaseCleanup;
import com.example.TaskAPI.core.SyncAuditExecutorConfig;
import com.example.TaskAPI.infrastructure.config.TestcontainersConfig;
import com.example.TaskAPI.security.JwtIssuer;
import com.example.TaskAPI.security.JwtProperties;
import com.example.TaskAPI.security.JwtSigningProperties;
import com.example.TaskAPI.security.JwtVerifier;
import com.example.TaskAPI.user.domain.entity.User;
import com.example.TaskAPI.user.domain.repository.UserRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import(SyncAuditExecutorConfig.class)
public abstract class BaseIntegrationTest extends TestcontainersConfig {
    private static final String TEST_SIGNING_KEY =
            "MC4CAQAwBQYDK2VwBCIEINasgH3eq32ySdu7p/4/dWyZURAunxRaLOHJ18DogBnU";

    protected static String testUserToken;
    protected static User loginUser;
    private final List<Long> deleteUserIds = new ArrayList<>();

    @Autowired
    protected UserRepository userRepository;
    @Autowired
    protected JwtProperties jwtProperties;
    @Autowired
    protected ObjectMapper objectMapper;
    protected JwtIssuer jwtIssuer;
    @Autowired
    private BaseDatabaseCleanup databaseCleanup;

    @BeforeAll
    void setupData() {
        jwtIssuer = new JwtIssuer(new JwtSigningProperties(TEST_SIGNING_KEY, Duration.ofHours(1)), jwtProperties);
        loginUser = persistUser("test-user");
        testUserToken = jwtIssuer.issue(loginUser.getId(), loginUser.getUsername(), List.of(JwtVerifier.DEFAULT_ROLE));
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
        User user = persistUser(username);
        deleteUserIds.add(user.getId());
        return user;
    }

    private User persistUser(String username) {
        return userRepository.save(User.builder()
                .username(username)
                .password("password")
                .build());
    }
}
