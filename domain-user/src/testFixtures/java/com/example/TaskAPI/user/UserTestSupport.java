package com.example.TaskAPI.user;

import com.example.TaskAPI.user.domain.entity.User;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

public interface UserTestSupport {
    TestEntityManager entityManager();

    default User createUser(String username) {
        return entityManager().persistAndFlush(User.builder()
                .username(username)
                .password("password")
                .build());
    }
}
