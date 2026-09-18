package com.example.TaskAPI.redis;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

public final class RedisTestContainer {
    private static final int REDIS_PORT = 6379;
    private static final GenericContainer<?> CONTAINER =
            new GenericContainer<>(DockerImageName.parse("redis:7-alpine")).withExposedPorts(REDIS_PORT);

    static {
        CONTAINER.start();
    }

    private RedisTestContainer() {
    }

    public static void register(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", CONTAINER::getHost);
        registry.add("spring.data.redis.port", () -> CONTAINER.getMappedPort(REDIS_PORT));
    }
}
