package com.example.TaskAPI.redis;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

public final class RedisTestContainer {
    private static final int RedisPort = 6379;
    private static final GenericContainer<?> Container =
            new GenericContainer<>(DockerImageName.parse("redis:7-alpine")).withExposedPorts(RedisPort);

    static {
        Container.start();
    }

    private RedisTestContainer() {
    }

    public static void register(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", Container::getHost);
        registry.add("spring.data.redis.port", () -> Container.getMappedPort(RedisPort));
    }
}
